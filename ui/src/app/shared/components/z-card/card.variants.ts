import { cva } from 'class-variance-authority';


export const cardVariants = cva('flex flex-col rounded-lg border border-t-highlight bg-card text-card-foreground shadow-sm/10 w-full p-6');

export const cardHeaderVariants = cva('flex flex-col space-y-1.5 pb-0 gap-1.5 mb-6', {
  variants: {},
});

export const cardActionVariants = cva('col-start-2 row-span-2 row-start-1 self-start justify-self-end');

export const cardBodyVariants = cva('flex flex-col flex-1 min-h-0');

export const cardFooterVariants = cva('flex flex-col gap-2 items-center px-6 [.border-t]:pt-6');
