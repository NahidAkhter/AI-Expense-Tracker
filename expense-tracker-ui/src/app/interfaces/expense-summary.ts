export interface ExpenseSummary {
    total: number;
    categoryBreakdown: { [category: string]: number };
    monthlyTrend: { month: string; amount: number }[];
}
