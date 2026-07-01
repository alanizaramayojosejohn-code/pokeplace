export interface DailySalesReport {
  date: string
  orderCount: number
  totalSales: number
}

export interface ProductReport {
  productName: string
  quantitySold: number
  revenue: number
}

export interface PaymentMethodReport {
  paymentMethod: string
  orderCount: number
  total: number
}

export interface MonthlyReport {
  month: number
  orderCount: number
  totalSales: number
  totalProfit: number
}

export interface UserSalesReport {
  userId: number
  userName: string
  orderCount: number
  totalSales: number
}

export interface ClientSalesReport {
  clientId: number
  clientName: string
  orderCount: number
  totalSales: number
}

export interface SalesReportSummary {
  periodTotal: number
  totalOrders: number
  dailySales: DailySalesReport[]
  bestSellingProducts: ProductReport[]
  salesByPaymentMethod: PaymentMethodReport[]
}

export interface DashboardReport {
  todaySales: number
  yesterdaySales: number
  salesGrowthPercent: number
  weekSales: number
  monthSales: number
  monthProfit: number
  todayOrders: number
  weekOrders: number
  monthOrders: number
  pendingOrders: number
  topProducts: ProductReport[]
  weeklyTrend: DailySalesReport[]
  salesByPaymentMethod: PaymentMethodReport[]
}

export interface HourlyReport {
  hour: number
  orderCount: number
  totalSales: number
}

export interface CategoryReport {
  categoryName: string
  quantitySold: number
  revenue: number
}

export interface YearComparison {
  year1: number
  year2: number
  year1Sales: number
  year2Sales: number
  year1Profit: number
  year2Profit: number
  salesGrowthPercent: number
  profitGrowthPercent: number
  year1Monthly: MonthlyReport[]
  year2Monthly: MonthlyReport[]
}

export interface InventoryValue {
  totalCostValue: number
  totalSaleValue: number
  potentialProfit: number
  totalProducts: number
  lowStockProducts: number
}

export interface SalesOrder {
  id: number
  tableNumber: number
  dateTime: string
  paymentMethod: string
  status: string
  total: number
  amountPaid: number | null
  change: number | null
  notes: string | null
  clientId: number | null
  clientName: string | null
  userId: number | null
  userName: string | null
}
