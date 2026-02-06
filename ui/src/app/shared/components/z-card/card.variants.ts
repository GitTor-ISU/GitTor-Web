import { cva, VariantProps } from 'class-variance-authority';

export const cardVariants = cva('block rounded-lg border border-t-highlight bg-card text-card-foreground shadow-sm/10 w-full p-6', {
  variants: {},
});
export type ZardCardVariants = VariantProps<typeof cardVariants>;

export const cardHeaderVariants = cva('flex flex-col space-y-1.5 pb-0 gap-1.5 mb-6', {
  variants: {},
});
export type ZardCardHeaderVariants = VariantProps<typeof cardHeaderVariants>;

export const cardBodyVariants = cva('block', {
  variants: {},
});
export type ZardCardBodyVariants = VariantProps<typeof cardBodyVariants>;
