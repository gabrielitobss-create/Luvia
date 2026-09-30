import {NativeModules} from 'react-native';

const nativeUrl = NativeModules.LuviaBackendConfig?.backendUrl as string | undefined;
export const backendUrl = (nativeUrl || 'http://10.0.2.2:8080/').replace(/\/$/, '');
