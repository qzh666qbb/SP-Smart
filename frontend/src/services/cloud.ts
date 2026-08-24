export async function callWithFallback<T = any>(
  apiFn: () => Promise<T>,
  _mockFn?: (data?: Record<string, any>) => T,
  _data?: Record<string, any>
): Promise<T> {
  return await apiFn();
}
