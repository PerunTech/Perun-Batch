import pkg from '../package.json'
import { pluginManager } from 'perun-core';
import * as plugin from './index';

// Router reads the routes off window[<name>], so this entry exports them as index.js does.
export * from './index';

pluginManager.registerPlugin(pkg.name, plugin);