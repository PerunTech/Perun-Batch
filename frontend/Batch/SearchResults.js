import { React, IntlContext, connect, ExportableGrid, GridManager, ComponentManager } from 'perun-core'

class SearchResults extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      grid: null
    }
    this.onRowClick = this.onRowClick.bind(this);
  }

  componentDidMount() {
    this.generateGridData()
  }

  componentWillUnmount() {
    ComponentManager.cleanComponentReducerState(`SVAROG_BATCH_JOB_${this.props.batch_type + this.props.templateId}`);
  }

  generateGridData() {

    const { batch_type, templateId, svSession, batchJobParamDisplay } = this.props
    const gridType = 'SVAROG_BATCH_JOB'
    const id = batch_type + templateId
    const gridId = gridType + '_' + id
    const showParamsLabel = this.context.formatMessage({ id: 'perun.batch.show_params', defaultMessage: 'perun.batch.show_params' })
    GridManager.reloadGridData(gridId)
    const grid = (
      <ExportableGrid
        gridType='READ_URL'
        key={gridId}
        id={gridId}
        configTableName={`/ReactElements/getTableFieldList/${svSession}/${gridType}`}
        dataTableName={`/ReactElements/getTableWithFilter/${svSession}/${gridType}/TEMPLATE_ID/${templateId}/10000`}
        onRowClickFunct={this.onRowClick}
        toggleCustomButton
        customButton={batchJobParamDisplay}
        customButtonLabel={showParamsLabel}
      />
    )
    ComponentManager.setStateForComponent(gridId, null, {
      onRowClickFunct: this.onRowClick,
      customButton: batchJobParamDisplay
    })
    this.setState({ grid })
  }

  onRowClick(_gridId, _rowId, row) {
    const batchStatus = row['SVAROG_BATCH_JOB.STATUS']
    const batchObjId = row['SVAROG_BATCH_JOB.OBJECT_ID']
    const batchNumRecords = row['SVAROG_BATCH_JOB.NUM_RECORDS']
    const batchName = row['SVAROG_BATCH_JOB.NAME']
    this.props.getRowId(batchStatus, batchObjId, batchNumRecords, batchName)
  }

  render() {
    const { grid } = this.state
    return (
      <div id='gridHolder'>
        {grid}
      </div>
    )
  }
}

const mapStateToProps = state => ({
  svSession: state.security.svSession
})

SearchResults.contextType = IntlContext

export default connect(mapStateToProps)(SearchResults)
