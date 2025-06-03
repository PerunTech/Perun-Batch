/**
 * Import all internal indexes, thus including the code in the final build.
 * export all content representing the surface of your plugin API. Noone is expected to call, but wth.
 * Wait to be called for render, Core will call you.
 */
import * as assets from './assets'; // eslint-disable-line
import SearchResults from './Batch/SearchResults';
import BatchWrapper from './Batch/BatchWrapper';

const routes = [{
  name: 'batch',
  path: '/main/perun-batch',
  render: BatchWrapper,
  isExact: true
}]

export { BatchWrapper, SearchResults, routes };