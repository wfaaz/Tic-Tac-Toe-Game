// tests/simulation.spec.js
const { test, expect } = require('@playwright/test');

test.describe('Tic-Tac-Toe Frontend E2E Integration Flow', () => {

  test('should simulate full automated game flow, display boards and show the outcome', async ({ page }) => {
    
    const mockSessionId = "test-session-999";
    const playerX = "player-abc";
    const playerO = "player-xyz";

    // 1. Intercept POST /sessions API (Creating session)
    await page.route('**/sessions', async route => {
      const json = {
        sessionId: mockSessionId,
        playerXId: playerX,
        playerOId: playerO,
        status: "CREATED",
        gameResult: "IN_PROGRESS"
      };
      await route.fulfill({ json, status: 201 });
    });

    // 2. Intercept POST /sessions/test-session-999/simulate API (Simulating gameplay turns)
    await page.route(`**/sessions/${mockSessionId}/simulate`, async route => {
      const json = {
        sessionId: mockSessionId,
        playerXId: playerX,
        playerOId: playerO,
        status: "FINISHED",
        gameResult: "X_WON",
        moveHistory: [
          { playerId: playerX, boardRow: 0, boardCol: 0, symbol: "X", timestamp: new Date().toISOString() },
          { playerId: playerO, boardRow: 1, boardCol: 0, symbol: "O", timestamp: new Date().toISOString() },
          { playerId: playerX, boardRow: 0, boardCol: 1, symbol: "X", timestamp: new Date().toISOString() },
          { playerId: playerO, boardRow: 1, boardCol: 1, symbol: "O", timestamp: new Date().toISOString() },
          { playerId: playerX, boardRow: 0, boardCol: 2, symbol: "X", timestamp: new Date().toISOString() } // Winning move
        ]
      };
      await route.fulfill({ json, status: 200 });
    });

    // 3. Navigate to the local server
    await page.goto('/');

    // Assert initial layout state
    await expect(page.locator('#session-id-display')).toHaveText('None');
    await expect(page.locator('#game-status')).toHaveText('Idle');

    // 4. Trigger Simulation Flow
    const startButton = page.locator('#start-btn');
    await startButton.click();

    // Verify loading status
    //await expect(page.locator('#game-status')).toHaveText('Creating Session...');

    // Wait for mock API calls to resolve and update local session display
    await expect(page.locator('#session-id-display')).toHaveText('test-ses...');

    // 5. Verify the animation board frames played out (X Win at row 0)
    const cell00 = page.locator('#cell-0-0');
    const cell01 = page.locator('#cell-0-1');
    const cell02 = page.locator('#cell-0-2');
    const cell10 = page.locator('#cell-1-0');
    const cell11 = page.locator('#cell-1-1');

    // Due to the custom delay(800) in index.html, we wait for final board states
    await expect(cell00).toHaveText('X', { timeout: 6000 });
    await expect(cell10).toHaveText('O');
    await expect(cell01).toHaveText('X');
    await expect(cell11).toHaveText('O');
    await expect(cell02).toHaveText('X');

    // Assert css classes injected successfully
    await expect(cell02).toHaveClass(/cell-x/);

    // 6. Verify final logged results
    await expect(page.locator('#game-status')).toHaveText('X_WON');
    
    // Check that move logger printed the logs
    const moveLogs = page.locator('#move-history-container > div');
    await expect(moveLogs).toHaveCount(5);
    await expect(moveLogs.first()).toContainText('Play: X at [0, 0]');
  });
});