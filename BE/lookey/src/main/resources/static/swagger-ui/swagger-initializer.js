window.onload = function() {
  // Get URL parameter or use default
  const urlParams = new URLSearchParams(window.location.search);

  // 개발환경에서는 절대 URL 사용하여 Pet Store 문제 해결
  const baseUrl = window.location.protocol + '//' + window.location.host;
  const isDevEnvironment = window.location.pathname.startsWith('/dev');
  const defaultUrl = isDevEnvironment ? baseUrl + '/dev/v3/api-docs' : '/v3/api-docs';
  const apiUrl = urlParams.get('url') || defaultUrl;

  window.ui = SwaggerUIBundle({
    url: apiUrl,
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",
    configUrl: isDevEnvironment ? baseUrl + '/dev/v3/api-docs/swagger-config' : '/v3/api-docs/swagger-config',
    validatorUrl: ''
  });
};
