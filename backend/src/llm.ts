import OpenAI from 'openai';
import { z } from 'zod';

const ApiKeySchema = z.string().min(1);

export function getOpenAIClient() {
  const apiKey = ApiKeySchema.parse(process.env.OPENAI_API_KEY);
  return new OpenAI({ apiKey });
}

export const DailyInsightsInput = z.object({
  date: z.string().min(8),
  sleep: z.object({
    scoreAvg: z.number().min(0).max(100),
    durationAvgMinutes: z.number().min(0).max(24 * 60),
    disturbancesAvg: z.number().min(0).max(100),
    bedtimeConsistency: z.number().min(0).max(100).optional(),
  }),
  steps: z.object({
    stepsToday: z.number().min(0).max(200_000),
    weeklyAvg: z.number().min(0).max(200_000).optional(),
  }),
  screen: z.object({
    bedtimeOverridesThisWeek: z.number().min(0).max(50).optional(),
  }),
  goals: z.array(z.string().min(1)).max(10).default([]),
});

export type DailyInsightsInput = z.infer<typeof DailyInsightsInput>;

export const DailyInsightsOutput = z.object({
  suggestions: z
    .array(
      z.object({
        title: z.string().min(1).max(80),
        action: z.string().min(1).max(240),
        reason: z.string().min(1).max(240),
      })
    )
    .min(1)
    .max(3),
});

export type DailyInsightsOutput = z.infer<typeof DailyInsightsOutput>;

export const WeeklyReportInput = z.object({
  weekStart: z.string().min(8),
  weekEnd: z.string().min(8),
  sleep: z.object({
    scoreAvg: z.number().min(0).max(100),
    durationAvgMinutes: z.number().min(0).max(24 * 60),
    disturbancesTotal: z.number().min(0).max(1000),
  }),
  steps: z.object({
    total: z.number().min(0).max(2_000_000),
    avgPerDay: z.number().min(0).max(200_000),
  }),
  habits: z.object({
    completionRate: z.number().min(0).max(1),
    bestStreak: z.number().min(0).max(365).optional(),
  }),
  screen: z.object({
    overrides: z.number().min(0).max(200).optional(),
  }),
  goals: z.array(z.string().min(1)).max(10).default([]),
});

export type WeeklyReportInput = z.infer<typeof WeeklyReportInput>;

export const WeeklyReportOutput = z.object({
  headline: z.string().min(1).max(120),
  keyObservation: z.string().min(1).max(320),
  oneActionForNextWeek: z.string().min(1).max(240),
});

export type WeeklyReportOutput = z.infer<typeof WeeklyReportOutput>;

export const SleepReportInput = z.object({
  sleepRecords: z
    .array(
      z.object({
        date: z.string().min(8),
        score: z.number().min(0).max(100),
        durationMinutes: z.number().min(0).max(24 * 60),
        disturbances: z.number().min(0).max(100),
      })
    )
    .min(1)
    .max(30),
  steps: z.array(z.number().min(0).max(200_000)).max(30).default([]),
  goals: z.array(z.string().min(1)).max(10).default([]),
  userName: z.string().min(1).max(80).default('User'),
});

export type SleepReportInput = z.infer<typeof SleepReportInput>;

export const SleepReportOutput = z.object({
  weeklyScore: z.number().min(0).max(100),
  previousWeekScore: z.number().min(0).max(100),
  trend: z.enum(['improving', 'declining', 'stable']),
  patterns: z.array(z.string().min(1).max(220)).min(1).max(5),
  riskAssessment: z.string().min(1).max(420),
  recommendations: z.array(z.string().min(1).max(240)).min(1).max(5),
  highlights: z.object({
    bestNight: z.object({
      date: z.string().min(8),
      score: z.number().min(0).max(100),
      note: z.string().min(1).max(160),
    }),
    worstNight: z.object({
      date: z.string().min(8),
      score: z.number().min(0).max(100),
      note: z.string().min(1).max(160),
    }),
  }),
});

export type SleepReportOutput = z.infer<typeof SleepReportOutput>;

