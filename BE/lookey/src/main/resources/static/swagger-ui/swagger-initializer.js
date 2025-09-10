window.onload = function() {
  // Get URL parameter or use default
  const urlParams = new URLSearchParams(window.location.search);
  const apiUrl = urlParams.get('url') || '/v3/api-docs';
  
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
    configUrl: '/v3/api-docs/swagger-config',
    validatorUrl: ''
  });
};