import { type Page } from '@playwright/test';

export async function addBook(page: Page, title: string): Promise<void> {
  await page.goto('/app/books');
  await page.getByLabel('Book title').fill(title);
  await page.getByRole('button', { name: 'Add' }).click();
  await page.getByRole('listitem').filter({ hasText: title }).waitFor();
}
