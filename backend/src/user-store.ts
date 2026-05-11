import { promises as fs } from 'node:fs';
import path from 'node:path';
import { z } from 'zod';

const ChallengeSchema = z.object({
  id: z.string().min(1),
  title: z.string().min(1).max(120),
  category: z.string().min(1).max(40),
  durationDays: z.number().int().min(1).max(120),
  successCriteria: z.string().min(1).max(400),
  startEpochDay: z.number().int(),
  createdAtMs: z.number().int(),
  completedToday: z.boolean().default(false),
});

const HabitSchema = z.object({
  id: z.string().min(1),
  type: z.enum(['pre_sleep', 'morning']),
  title: z.string().min(1).max(120),
  reminderMinutesOfDay: z.number().int().min(0).max(24 * 60 - 1).nullable(),
  enabled: z.boolean(),
  completedToday: z.boolean().default(false),
});

const ProfileSchema = z.object({
  name: z.string().min(1).max(80).nullable().default(null),
  email: z.string().email().nullable().default(null),
});

const SpotifyTokenSchema = z.object({
  accessToken: z.string().min(1),
  refreshToken: z.string().min(1).nullable().default(null),
  expiresAtMs: z.number().int().min(0),
  scope: z.string().nullable().default(null),
});

const UserRecordSchema = z.object({
  profile: ProfileSchema.default({ name: null, email: null }),
  habits: z.array(HabitSchema).default([]),
  challenges: z.array(ChallengeSchema).default([]),
  spotify: SpotifyTokenSchema.nullable().default(null),
  updatedAt: z.string().datetime().default(() => new Date().toISOString()),
});

const StoreSchema = z.object({
  users: z.record(z.string(), UserRecordSchema).default({}),
});

export type HabitRecord = z.infer<typeof HabitSchema>;
export type ChallengeRecord = z.infer<typeof ChallengeSchema>;
type UserRecord = z.infer<typeof UserRecordSchema>;
type Store = z.infer<typeof StoreSchema>;

const defaultHabits: HabitRecord[] = [
  {
    id: 'habit-pre-sleep-brush',
    type: 'pre_sleep',
    title: 'Brush teeth',
    reminderMinutesOfDay: 22 * 60,
    enabled: true,
    completedToday: false,
  },
  {
    id: 'habit-pre-sleep-water',
    type: 'pre_sleep',
    title: 'Drink water',
    reminderMinutesOfDay: 22 * 60 + 10,
    enabled: true,
    completedToday: false,
  },
  {
    id: 'habit-morning-hydration',
    type: 'morning',
    title: 'Hydration',
    reminderMinutesOfDay: 7 * 60 + 10,
    enabled: true,
    completedToday: false,
  },
];

export class UserStore {
  private readonly filePath: string;

  constructor(baseDir = process.cwd()) {
    this.filePath = path.join(baseDir, '.data', 'user-store.json');
  }

  private async load(): Promise<Store> {
    try {
      const raw = await fs.readFile(this.filePath, 'utf8');
      return StoreSchema.parse(JSON.parse(raw));
    } catch {
      return { users: {} };
    }
  }

  private async save(store: Store) {
    await fs.mkdir(path.dirname(this.filePath), { recursive: true });
    await fs.writeFile(this.filePath, JSON.stringify(store, null, 2), 'utf8');
  }

  private ensureUserRecord(store: Store, userId: string): UserRecord {
    const existing = store.users[userId];
    if (existing) return existing;
    const created = UserRecordSchema.parse({
      habits: defaultHabits,
      challenges: [],
      updatedAt: new Date().toISOString(),
    });
    store.users[userId] = created;
    return created;
  }

  async getUser(userId: string) {
    const store = await this.load();
    return this.ensureUserRecord(store, userId);
  }

  async updateProfile(userId: string, name: string | null, email: string | null) {
    const store = await this.load();
    const user = this.ensureUserRecord(store, userId);
    user.profile = {
      name: name?.trim() || null,
      email: email?.trim() || null,
    };
    user.updatedAt = new Date().toISOString();
    await this.save(store);
    return user.profile;
  }

  async replaceChallenges(userId: string, challenges: ChallengeRecord[]) {
    const validated = z.array(ChallengeSchema).parse(challenges);
    const store = await this.load();
    const user = this.ensureUserRecord(store, userId);
    user.challenges = validated;
    user.updatedAt = new Date().toISOString();
    await this.save(store);
    return user.challenges;
  }

  async replaceHabits(userId: string, habits: HabitRecord[]) {
    const validated = z.array(HabitSchema).parse(habits);
    const store = await this.load();
    const user = this.ensureUserRecord(store, userId);
    user.habits = validated;
    user.updatedAt = new Date().toISOString();
    await this.save(store);
    return user.habits;
  }

  async setSpotifyToken(
    userId: string,
    token: { accessToken: string; refreshToken: string | null; expiresAtMs: number; scope: string | null }
  ) {
    const store = await this.load();
    const user = this.ensureUserRecord(store, userId);
    user.spotify = SpotifyTokenSchema.parse(token);
    user.updatedAt = new Date().toISOString();
    await this.save(store);
    return user.spotify;
  }

  async clearSpotify(userId: string) {
    const store = await this.load();
    const user = this.ensureUserRecord(store, userId);
    user.spotify = null;
    user.updatedAt = new Date().toISOString();
    await this.save(store);
  }
}
