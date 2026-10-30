// The backend charges in USD (see PurchaseService).
export function formatPrice(price) {
  return Number(price) === 0 ? 'Free' : `$${Number(price).toFixed(2)}`
}

export function formatDate(iso) {
  return new Date(iso).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}
