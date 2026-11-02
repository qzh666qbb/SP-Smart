export interface Page<T> {
  items: T[];
  total: number;
}

// 按服务端总数继续取下一页，不以单页条数代替完整列表。
export async function collectPages<T>(
  fetchPage: (pageNum: number) => Promise<Page<T>>,
  isCurrent: () => boolean = () => true,
): Promise<T[]> {
  const items: T[] = [];
  for (let pageNum = 1; isCurrent(); pageNum++) {
    const page = await fetchPage(pageNum);
    if (!isCurrent()) return items;
    if (page.items.length === 0 && items.length < page.total) {
      throw new Error('列表数据发生变化，请重新加载');
    }
    items.push(...page.items);
    if (items.length >= page.total) break;
  }
  return items;
}
