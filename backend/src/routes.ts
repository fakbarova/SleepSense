import { z } from 'zod';

export const RouteSuggestInput = z.object({
  origin: z.object({ lat: z.number(), lng: z.number() }),
  destination: z.object({ lat: z.number(), lng: z.number() }),
  stepsRemaining: z.number().min(0).max(200_000),
});

export type RouteSuggestInput = z.infer<typeof RouteSuggestInput>;

export const RouteSuggestOutput = z.object({
  suggestion: z.object({
    summary: z.string(),
    extraSteps: z.number().min(0),
    extraMinutes: z.number().min(0),
    distanceMeters: z.number().min(0),
    durationSeconds: z.number().min(0),
    encodedPolyline: z.string().min(1),
  }),
});

export type RouteSuggestOutput = z.infer<typeof RouteSuggestOutput>;

