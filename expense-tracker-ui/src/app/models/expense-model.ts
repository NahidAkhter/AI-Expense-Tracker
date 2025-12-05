import { Expense } from "../interfaces/expense";

export class ExpenseModel implements Expense {
  constructor(
    public description: string,
    public amount: number,
    public date: string,
    public category: string,
    public id?: number,
    public aiInsights?: string
  ) {}

  static fromJson(json: any): ExpenseModel {
    return new ExpenseModel(
      json.description,
      json.amount,
      json.date,
      json.category,
      json.id,
      json.aiInsights
    );
  }

  toJson(): any {
    return {
      id: this.id,
      description: this.description,
      amount: this.amount,
      date: this.date,
      category: this.category,
      aiInsights: this.aiInsights
    };
  }
}
