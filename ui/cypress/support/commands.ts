Cypress.Commands.add('getBySel', (selector, ...args) => {
  return cy.get(`[data-test=${selector}]`, ...args);
});

Cypress.Commands.add('login', (username, password) => {
  const loginPath = '/login';

  cy.intercept('POST', '/api/authenticate/login').as('login');
  cy.intercept('GET', '/api/users/me').as('me');
  cy.visit(loginPath);

  if (username) cy.getBySel('login-username').type(username);
  if (password) cy.getBySel('login-password').type(password);

  cy.getBySel('login-submit').then(($btn) => {
    if ($btn.is(':disabled')) return;
    cy.wrap($btn).click();
    cy.wait('@login').then((interception) => {
      if (interception.response?.statusCode === 200) {
        cy.wait('@me');
      }
    });
  });
});

Cypress.Commands.add('register', (username, email, password, confirmPassword?) => {
  const registerPath = '/register';

  cy.intercept('POST', '/api/authenticate/register').as('register');
  cy.intercept('GET', '/api/users/me').as('me');
  cy.visit(registerPath);

  if (username) cy.getBySel('register-username').type(username);
  if (email) cy.getBySel('register-email').type(email);
  if (password) cy.getBySel('register-password').type(password);
  if (confirmPassword || password) cy.getBySel('register-confirm-password').type(confirmPassword || password);

  cy.getBySel('register-submit').then(($btn) => {
    if ($btn.is(':disabled')) return;
    cy.wrap($btn).click();
    cy.wait('@register').then((interception) => {
      if (interception.response?.statusCode === 200) {
        cy.wait('@me');
      }
    });
  });
});

Cypress.Commands.add('verifyLogin', () => {
  cy.visit('');

  return cy.get('body').then(($body) => {
    return $body.find('[data-test="sidebar-logout-button"]').length > 0;
  });
});
