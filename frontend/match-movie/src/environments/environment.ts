export const environment = {
  production: true,
  apiUrl: '',
  auth: {
    authority: '',
    redirectUrl: window.location.origin,
    postLogoutRedirectUri: window.location.origin,
    clientId: '',
    scope: 'openid profile email',
    responseType: 'code',
    silentRenew: true,
    useRefreshToken: true,
    logLevel: 2,
  },
};
