export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  auth: {
    authority: 'http://localhost:8180/realms/homelab',
    redirectUrl: window.location.origin,
    postLogoutRedirectUri: window.location.origin,
    clientId: 'match-movie-client',
    scope: 'openid profile email',
    responseType: 'code',
    silentRenew: true,
    useRefreshToken: true,
    logLevel: 0,
  },
};
