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