Cypress.Commands.add('getBySel', (selector, ...args) => {
  return cy.get(`[data-test=${selector}]`, ...args);
});

Cypress.Commands.add('login', (username, password) => {
  const loginPath = '/login';

  cy.intercept('POST', '/api/authenticate/login').as('login');
  cy.visit(loginPath);

  if (username) cy.getBySel('login-username').type(username);
  if (password) cy.getBySel('login-password').type(password);

  cy.getBySel('login-submit').then(($btn) => {
    if ($btn.is(':disabled')) return;
    cy.wrap($btn).click();
    cy.wait('@login');
  });
});

Cypress.Commands.add('register', (username, email, password, confirmPassword?) => {
  const registerPath = '/register';

  cy.intercept('POST', '/api/authenticate/register').as('register');
  cy.visit(registerPath);

  if (username) cy.getBySel('register-username').type(username);
  if (email) cy.getBySel('register-email').type(email);
  if (password) cy.getBySel('register-password').type(password);
  if (confirmPassword || password) cy.getBySel('register-confirm-password').type(confirmPassword || password);

  cy.getBySel('register-submit').then(($btn) => {
    if ($btn.is(':disabled')) return;
    cy.wrap($btn).click();
    cy.wait('@register');
  });
});

Cypress.Commands.add('verifyLogin', () => {
  cy.visit('');
  cy.getBySel('navbar-user-button').click();

  return cy.get('body').then(($body) => {
    return $body.find('[data-test="navbar-logout-button"]').length > 0;
  });
});
