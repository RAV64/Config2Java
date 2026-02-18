def mode = (ENV.DATA2JAVA_TEST_APP_ENV_6A0C9341_EE7F_4F5A_BA4A == 'prod') ? 'PROD' : 'DEV'
return [mode: mode, name: defaultName]
