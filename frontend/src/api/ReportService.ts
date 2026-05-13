import api from '@/api/axios'

import type { SalesReportSummary } from '@/types/report.ts'

export const reportService = {
  getSummary(start: string, end: string) {
    return api.get<SalesReportSummary>('/reports/summary', {
      params: { start, end }
    })
  }
}


