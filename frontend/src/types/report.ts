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

export interface SalesReportSummary {
  periodTotal: number
  totalOrders: number
  dailySales: DailySalesReport[]
  bestSellingProducts: ProductReport[]
  salesByPaymentMethod: PaymentMethodReport[]
}