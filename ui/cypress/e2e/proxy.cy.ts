/// <reference types="cypress" />

context('Proxy', () => {
  /**
   * Attempt to access the API via the proxy
   */
  it('Heartbeat', () => {
    cy.request('/api').then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.eq(true);
    });
  });
});
