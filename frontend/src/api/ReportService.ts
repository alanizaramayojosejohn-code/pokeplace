import api from '@/api/axios'

import type {
  ClientSalesReport,
  DashboardReport,
  HourlyReport,
  CategoryReport,
  MonthlyReport,
  SalesReportSummary,
  UserSalesReport,
  YearComparison,
  InventoryValue,
  SalesOrder
} from '@/types/report.ts'

export const reportService = {
  getDashboard() {
    return api.get<DashboardReport>('/reports/dashboard')
  },

  getSummary(start: string, end: string) {
    return api.get<SalesReportSummary>('/reports/summary', {
      params: { start, end }
    })
  },

  getMonthly(year: number) {
    return api.get<MonthlyReport[]>('/reports/monthly', {
      params: { year }
    })
  },

  getHourlySales(start: string, end: string) {
    return api.get<HourlyReport[]>('/reports/hourly', {
      params: { start, end }
    })
  },

  getSalesByCategory(start: string, end: string) {
    return api.get<CategoryReport[]>('/reports/categories', {
      params: { start, end }
    })
  },

  getYearComparison(year1: number, year2: number) {
    return api.get<YearComparison>('/reports/comparison', {
      params: { year1, year2 }
    })
  },

  getInventoryValue() {
    return api.get<InventoryValue>('/reports/inventory-value')
  },

  downloadExcel(year: number) {
    return api.get('/reports/export/excel', {
      params: { year },
      responseType: 'blob'
    })
  },

  downloadPdf(year: number) {
    return api.get('/reports/export/pdf', {
      params: { year },
      responseType: 'blob'
    })
  },

  getSalesByUser(start: string, end: string) {
    return api.get<UserSalesReport[]>('/reports/sales-by-user', {
      params: { start, end }
    })
  },

  getSalesByClient(start: string, end: string) {
    return api.get<ClientSalesReport[]>('/reports/sales-by-client', {
      params: { start, end }
    })
  },

  downloadSummaryExcel(start: string, end: string) {
    return api.get('/reports/export/summary/excel', {
      params: { start, end },
      responseType: 'blob'
    })
  },

  downloadSummaryPdf(start: string, end: string) {
    return api.get('/reports/export/summary/pdf', {
      params: { start, end },
      responseType: 'blob'
    })
  },

  getSalesOrders(start: string, end: string) {
    return api.get<SalesOrder[]>('/reports/orders', {
      params: { start, end }
    })
  }
}
