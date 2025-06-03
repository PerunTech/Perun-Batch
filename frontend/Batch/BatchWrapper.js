import * as config from '../config/config'
import SearchResults from './SearchResults'
import queryString from 'query-string';
import { React, connect, elements, axios, PropTypes, createHashHistory, ComponentManager, ExportableGrid, GridManager, Modal, Loading } from 'perun-core'
import Statistics from '../Statistics/Statistics'
import { iconManager } from '../assets/svgHolder'
const { Dropdown, InputElement, alertUserV2, alertUserResponse, ReactBootstrap } = elements
const { Modal: ReactBootstrapModal } = ReactBootstrap

const initialState = {
  process: '',
  num: '',
  comment: '',
  option: ''
}

class BatchWrapper extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      initialState,
      batchJson: [],
      showStart: false,
      showStop: false,
      batchJsonTemp: '',
      appTypes: '',
      batchtemplate: '',
      jobId: '',
      selectedApps: 0,
      showFormProcess: false,
      showTablePreview: false,
      showTable: false,
      showDownloadZipBtn: false,
      templateId: '',
      showRemoveFromSelection: false,
      removeIdbr: '',
      formRemoveIDBR: false,
      showButton: false,
      showReport: false,
      showActionsBtn: false,
      showStatsHtml: false,
      showStatsState: false,
      processWithErr: false,
    }
    this.getAppTypes = this.getAppTypes.bind(this)
    this.getRowId = this.getRowId.bind(this)
    this.submitJobParams = this.submitJobParams.bind(this)
    this.selectObjects = this.selectObjects.bind(this)
    this.runJobSelection = this.runJobSelection.bind(this)
    this.stopJob = this.stopJob.bind(this)
    this.createJsonBatchAfsardTmp = this.createJsonBatchAfsardTmp.bind(this)
    this.getJobTemp = this.getJobTemp.bind(this)
    this.inputParams = this.inputParams.bind(this)
    this.inputAndSearch = this.inputAndSearch.bind(this)
    this.showSearchResult = this.showSearchResult.bind(this)
    this.showCreateForm = this.showCreateForm.bind(this)
    this.onChange = this.onChange.bind(this)
    this.onReset = this.onReset.bind(this)
    this.downloadZip = this.downloadZip.bind(this)
    this.closeModal = this.closeModal.bind(this)
    this.saveSelectedIdbr = this.saveSelectedIdbr.bind(this)
    this.iterateBatchDataTemplate = this.iterateBatchDataTemplate.bind(this)
    this.getActions = this.getActions.bind(this)
    this.iterateActions = this.iterateActions.bind(this)
    this.generatePayment = this.generatePayment.bind(this)
    this.showModal = this.showModal.bind(this)
    this.getReport = this.getReport.bind(this)
    this.iterateReport = this.iterateReport.bind(this)
    this.showReport = this.showReport.bind(this)
    this.deleteActionModal = this.deleteActionModal.bind(this)
    this.showActiveProcessStats = this.showActiveProcessStats.bind(this)
    this.iterateStats = this.iterateStats.bind(this)
    this.hashHistory = createHashHistory()
    this.closeLoading = this.closeLoading.bind(this)
    this.batchJobParamDisplay = this.batchJobParamDisplay.bind(this)
    this.showDisplayBatchJobParamModal = this.showDisplayBatchJobParamModal.bind(this)
    this.startProcess = this.startProcess.bind(this)
    this.closeDisplayBatchJobParamModal = this.closeDisplayBatchJobParamModal.bind(this)
    this.displayCadLocationGridModal = this.displayCadLocationGridModal.bind(this)
    this.closeCadLocationGridModal = this.closeCadLocationGridModal.bind(this)
    this.onCadLocationRowClick = this.onCadLocationRowClick.bind(this)
  }

  componentDidMount() {
    this.readUrlState(this.props.location.search);
    this.getJobType();
    if (document.getElementById('identificationScreen')) {
      document.getElementById('identificationScreen').className = 'identificationScreen'
      document.getElementById('identificationScreen').innerText = this.context.intl.formatMessage({
        id: 'perun.plugin.perun_batch', defaultMessage: 'perun.plugin.perun_batch'
      })
    }
  }

  /* read url and execute appropriate actions */
  readUrlState(urlContent) {
    let params = queryString.parse(urlContent)
    if (params.batch_type) {
      this.getJobType(params.batch_type);
      if (params.templateId) {
        this.getJobTemp(params.batch_type, params.templateId);
      } else {
        this.getJobTemp(params.batch_type, '');
      }
      if (params.action) {
        if (params.action === 'grid') {
          this.showSearchResult(params.batch_type, params.templateId);
        }
        if (params.action === 'createProcess') {
          this.showCreateForm()
          if (params.processName) {
            this.onChange('', params.processName, '', '')
          }
          if (params.processNumber) {
            this.onChange('', '', params.processNumber, '')
          }
          if (params.processNote) {
            this.onChange('', '', '', params.processNote)
          }
        }
        if (params.action === 'processInfo') {
          this.createJsonBatchAfsardTmp(params.processName, params.processNumber, params.processNote, params.batch_type, params.templateId)
        }
      }
    } else {
      this.getJobType();
    }
  }

  getJobType(batch_type) {
    const url = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.GET_BATCH_TYPE + this.props.svSession
    axios.get(url).then((response) => {
      if (response.data && Array.isArray(response.data) && response.data.length > 0) {
        this.iterateBatchDataTemplate(response.data, this.context.intl.formatMessage({
          id: 'perun.batch.choose_process_type', defaultMessage: 'perun.batch.choose_process_type'
        }), 1, '', batch_type)
      } else {
        if (response.data?.type === 'ERROR') {
          alertUserResponse({ response: response.data })
        }
      }
    }).catch((err) => {
      console.error(err)
      alertUserResponse({ response: err })
    })
  }

  /* check status of valid process and decide witch form/step to display */
  getRowId(batchStatus, batchObjId, batchNumRecords, batchName) {
    let component = this
    this.setState({ jobId: batchObjId, batchtemplate: '', batchName: batchName, processWithErr: false })

    switch (batchStatus) {
      case 'SEL_OBJ': {
        this.setState({ showFormProcess: true, showJson: false, selectedApps: batchNumRecords, showTemplate: false, showStop: false, showStart: false, showDownloadZipBtn: false, showReport: false, showActionsBtn: false, showStatsState: false, startJob: true, stopJob: false })
        break
      }
      case 'VALID': {
        component.getJobTemplate(component.state.templateId)
        component.setState({ showTemplate: false, showJson: false, showTable: false, showFormProcess: false, showStop: false, showStart: false, showDownloadZipBtn: false, showReport: false, showActionsBtn: false, showStatsState: false, startJob: false, stopJob: false })
        break
      }
      case 'STARTED': {
        clearInterval(this.interval)
        this.showActiveProcessStats(batchObjId, this.interval = setInterval(
          () => this.showActiveProcessStats(batchObjId)
          , 7400))
        this.setState({ showStop: true, showJson: false, showFormProcess: true, showStart: true, showTemplate: false, showDownloadZipBtn: false, showReport: false, showActionsBtn: false, batchStatus: batchStatus, startJob: false, stopJob: true })
        break
      }
      case 'STOPPED': {
        this.showActiveProcessStats(batchObjId)
        this.setState({ showStop: true, showJson: false, showStart: true, showTemplate: false, showFormProcess: true, showDownloadZipBtn: false, showReport: false, showActionsBtn: false, startJob: true, stopJob: false })
        break
      }
      case 'FINISHED': {
        this.getActions(batchObjId)
        this.getReport(batchObjId)
        this.showActiveProcessStats(batchObjId, 'FINISHED')
        this.setState({ showTemplate: false, showFormProcess: false, showJson: false, showStop: false, showStart: false, showDownloadZipBtn: true, formRemoveIDBR: true, batchStatus: batchStatus, startJob: false, stopJob: false })
        break
      }
      default: {
        this.setState({ showTemplate: false, showFormProcess: false, showJson: true, showStop: false, showStart: false, showDownloadZipBtn: false, showReport: false, showActionsBtn: false, showStatsState: false, startJob: false, stopJob: false })
        break
      }
    }
  }

  iterateBatchDataTemplate(data, select_label, num_drop, name_field, predefValue) {
    let configHtmlBuffer = []
    let htmlElementBatchTmp
    let fieldName = "batch_type"
    let fieldNameTemp = "templateId"

    if (num_drop === 1) {
      htmlElementBatchTmp = <div key='fieldName' ><label>{this.context.intl.formatMessage({ id: 'perun.batch.choose_process_type', defaultMessage: 'perun.batch.choose_process_type' })}</label><Dropdown
        id={fieldName}
        name={fieldName}
        value={predefValue}
        onChange={this.getJobTemp}
        options={data}
      /></div>
    }

    if (num_drop === 2) {
      htmlElementBatchTmp = <div key='fieldNameTemp' ><label>{this.context.intl.formatMessage({ id: 'perun.batch.choose_process', defaultMessage: 'perun.batch.choose_process' })}</label><Dropdown
        id={fieldNameTemp}
        name={fieldNameTemp}
        value={predefValue}
        onChange={this.inputAndSearch}
        options={data}
      /></div>
    }

    if (num_drop === 3) {
      htmlElementBatchTmp = <Dropdown
        id={name_field}
        name={name_field}
        onChange={this.inputParams}
        options={data}
      />
    }
    configHtmlBuffer.push(htmlElementBatchTmp)

    if (num_drop === 1) {
      this.setState({ batchJson: configHtmlBuffer })
    }

    if (num_drop === 2) {
      this.setState({ batchJsonTemp: configHtmlBuffer })
    }

    if (num_drop === 3) {
      return configHtmlBuffer
    }
  }

  getJobTemplate(templateId) {
    const component = this
    const url = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.GET_PARAMS_TEMPLATES + component.props.svSession + '/' + templateId
    axios.get(url).then((response) => {
      if (response.data.data.length > 0) {
        response.data.data.forEach(template => {
          if (template.name === 'Ид на модул') {
            component.getAppTypes(response.data)
          } else if (template.type === 'dropdown') {
            component.getCodeList(template.code_list, response.data)
          } else {
            component.createJsonTemp(response.data)
          }
        })
      } else {
        component.selectObjects()
      }
    }).catch((err) => {
      console.error(err)
      alertUserResponse({ response: err })
    })
  }

  getAppTypes(dataTmp) {
    this.setState({ loading: <Loading /> })
    let component = this
    const url = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.GET_DROP_DOWN_VALUES + component.props.svSession + '/APPLICATION_TYPE/REGIONAL_OFFICE/2019'
    axios.get(url).then((response) => {
      if (response.data) {
        component.setState({ loading: false }, () => component.createJsonTemp(dataTmp, response.data))
      }
    }).catch((err) => {
      console.error(err)
      this.setState({ loading: false })
      alertUserResponse({ response: err })
    })
  }

  getCodeList(codeListType, dataTmp) {
    const url = config.svConfig.restSvcBaseUrl + '/Iacs/getCodeList/' + this.props.svSession + `/${codeListType}`
    this.setState({ loading: <Loading /> })
    let component = this
    axios.get(url).then(res => {
      if (res.data) {
        this.setState({ loading: false, [codeListType]: res.data }, () => component.createJsonTemp(dataTmp))
      }
    }).catch(err => {
      console.error(err)
      component.setState({ loading: false })
      alertUserResponse({ response: err })
    })
  }

  displayCadLocationGridModal() {
    let cadLocationTableName = 'CAD_LOCATION'
    let cadLocationGridId = 'CAD_LOCATION_GRID'

    const grid = (
      <ExportableGrid
        gridType='READ_URL'
        key={cadLocationGridId}
        id={cadLocationGridId}
        configTableName={`/ReactElements/getTableFieldList/${this.props.svSession}/${cadLocationTableName}`}
        dataTableName={`/ReactElements/getTableData/${this.props.svSession}/${cadLocationTableName}/3000`}
        onRowClickFunct={this.onCadLocationRowClick}
        minHeight={600}
      />
    )

    const cadLocationGridModal = (
      <Modal
        modalTitle={'cad_location'}
        closeModal={this.closeCadLocationGridModal}
        modalSize={80}
        modalContent={grid}
        isSecondary
        forceCloseOnEsc
      />
    )
    this.setState({ cadLocationGridModal })
  }

  closeCadLocationGridModal() {
    let cadLocationGridId = 'CAD_LOCATION_GRID'
    this.setState({ cadLocationGridModal: undefined })
    ComponentManager.cleanComponentReducerState(cadLocationGridId)
  }

  onCadLocationRowClick(_id, _idx, row) {
    let cadLocationGridId = 'CAD_LOCATION_GRID'
    let cadLocationTableName = 'CAD_LOCATION'

    const selectedCadLocationMunicipality = row[`${cadLocationTableName}.MUNICIPALITY`]
    const columns = ComponentManager.getStateForComponent(cadLocationGridId, 'gridConfig')
    columns.forEach(column => {
      if (column.key === `${cadLocationTableName}.MUNICIPALITY`) {
        column.formatterOptions?.forEach(munic => {
          if (selectedCadLocationMunicipality === munic.value) {
            document.getElementById('cad_munic').value = munic.title;
            this.closeCadLocationGridModal()
          }
        })
      }
    })
  }

  createJsonTemp(dataTmp, dropDownVal) {
    let appTypesDropDownVal = dropDownVal?.appTypes
    let orgUntitsDropDownVal = dropDownVal?.orgUnits
    let jobParams = dataTmp.data
    let i
    let fieldType = 'textarea'
    let required
    let dropdownAppType = []
    let dropdownOrgUnit = []
    let configHtmlBuffer = []
    let htmlElementTemplate = []
    let createParamsbtn = []
    let formFieldId = []

    if (jobParams.length > 0) {
      for (i = 0; i < jobParams.length; i++) {
        if (jobParams[i].type === 'NUMERIC') {
          fieldType = 'number'
        } else {
          if (jobParams[i].type === 'ATTACHMENT') {
            fieldType = 'file'
          } else {
            fieldType = 'textarea'
          }
        }

        if (jobParams[i].mandatory) {
          required = jobParams[i].mandatory
          formFieldId.push(jobParams[i].id + '-' + jobParams[i].mandatory)
        }

        // if (jobParams[i].name === 'Процент случаен избор') {
        //   htmlElementTemplate = <input className={style['custom-form-elements']}
        //     type={fieldType} id={'cad_munic'} name={'cad_munic'} placeholder={'Притиснете за избор на катастарска општина'}
        //     onClick={this.displayCadLocationGridModal} />
        //   configHtmlBuffer.push(htmlElementTemplate)
        // } else {
        if (jobParams[i].name === 'Ид на модул') {
          dropdownAppType = this.iterateBatchDataTemplate(appTypesDropDownVal, 'Листа на модули', 3, 'application.app_type_id')
          configHtmlBuffer.push(dropdownAppType)
        } else {
          if (jobParams[i].name === 'Подрачна') {
            dropdownOrgUnit = this.iterateBatchDataTemplate(orgUntitsDropDownVal, 'Подрачни единици', 3, 'svarog_org_units.external_id')
            configHtmlBuffer.push(dropdownOrgUnit)
          } else {
            if (jobParams[i].type === 'ATTACHMENT') {
              htmlElementTemplate = <div className='import-holder'><label id='uploadLabel' className='upload-label'>{this.context.intl.formatMessage({ id: 'perun.batch.file_name', defaultMessage: 'perun.batch.file_name' })}</label><label className='custom-label' htmlFor='attachment' >{iconManager.getIcon('upload_file')} {this.context.intl.formatMessage({ id: 'perun.batch.upload_file', defaultMessage: 'perun.batch.upload_file' })}</label>
                <input className='custom-form-elements' type={fieldType} id={jobParams[i].id} name={jobParams[i].name} placeholder={jobParams[i].name}
                  onChange={(e) => this.inputParams(e, 'getUploadName')} required={required} /></div>
              configHtmlBuffer.push(htmlElementTemplate)
            } else {
              if (jobParams[i].type === 'dropdown') {
                let codeListValue
                let codeListValueDropDown = ''
                if (jobParams[i].code_list) {
                  codeListValue = jobParams[i].code_list
                  codeListValueDropDown = this.state[codeListValue]
                  htmlElementTemplate = <Dropdown
                    id={jobParams[i].id}
                    name={jobParams[i].name}
                    onChange={this.inputParams}
                    options={codeListValueDropDown}
                  />
                  configHtmlBuffer.push(htmlElementTemplate)
                } else {
                  console.warn('code list missing in json response')
                }
              } else {
                htmlElementTemplate = <input className='custom-form-elements'
                  type={fieldType} id={jobParams[i].id} name={jobParams[i].name} placeholder={jobParams[i].name}
                  onChange={this.inputParams} required={required} />
                configHtmlBuffer.push(htmlElementTemplate)
              }
            }
          }
        }
        //    }
      }
      createParamsbtn = <button className='batch-button button-create custom-form-elements button-left' id={'create_tmp'} type='submit' onClick={this.submitJobParams}>{this.context.intl.formatMessage({ id: 'perun.batch.enter_parameters', defaultMessage: 'perun.batch.enter_parameters' })}</button>
      configHtmlBuffer.push(createParamsbtn)
    }
    this.setState({ batchtemplate: configHtmlBuffer, showTemplate: true, formFieldId: formFieldId })
  }

  submitJobParams() {
    let checkArr = []
    for (let i = 0; i < this.state.formFieldId.length; i++) {
      if (this.state.formFieldId[i].split('-')[1]) {
        checkArr.push(this.state.formFieldId[i].split('-')[1])
      }
    }

    // return boolean if the fn parameter returns the matching condition
    function isFalse(fieldId) {
      return fieldId === 'false'
    }

    // check the condition for every value in the array that has been
    // manipulated depending on the values in input params func
    let result = checkArr.every(isFalse)

    // if the matchin result is true proceed with axios, else return alert
    if (result === true) {
      const component = this
      const restUrl = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.SAVE_BATCH_PARAMS
      let batch_params = component.state

      const data = new FormData() // eslint-disable-line

      /* if in state attachment add key for uploaded file */
      if (batch_params.attachment) {
        let file = component.state.attachment
        data.append(file.name, file)
      }

      data.append('param', JSON.stringify(batch_params))

      axios({
        method: 'post',
        data: data,
        url: restUrl + this.props.svSession,
        headers: { 'Content-Type': 'multipart/form-data' }
      }).then(function (response) {
        component.setState({ showTemplate: false })
        alertUserResponse({ response: response?.data })
        if (batch_params.attachment) {
          component.setState({ attachment: undefined })
        }
        if (response.data.type === 'SUCCESS') {
          component.selectObjects()
        }
      }).catch(function (err) {
        console.error(err)
        alertUserResponse({ response: err })
      })
    } else {
      alertUserV2({
        type: 'info',
        title: this.context.intl.formatMessage({ id: 'perun.batch.fill_in_the_form', defaultMessage: 'perun.batch.fill_in_the_form' }),
      })
    }
  }

  /* run selection */
  selectObjects() {
    const component = this
    alertUserV2({
      type: 'success',
      title: this.context.intl.formatMessage({ id: 'perun.batch.selection_started_please_wait', defaultMessage: 'perun.batch.selection_started_please_wait' }),
    })
    component.setState({ loading: <Loading /> })
    const restUrl = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.SELECTION_JOB
    let batch_params
    if (component.state.jobId) {
      batch_params = { "jobId": component.state.jobId }
    }
    axios({
      method: 'post',
      data: JSON.stringify(batch_params),
      url: restUrl + component.props.svSession,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }).then(function (response) {
      if (response?.data) {
        alertUserResponse({ response: response.data, onConfirm: () => component.closeLoading() })
        component.setState({ showFormProcess: true, startJob: true, stopJob: false, selectedApps: response.data.data.numSelectedApps })
        let showTablePreview = <SearchResults key={'SVAROG_BATCH_JOB_' + component.state.batch_type + component.state.templateId} templateId={component.state.templateId} batch_type={component.state.batch_type} getRowId={component.getRowId} batchJobParamDisplay={component.batchJobParamDisplay} />
        component.setState({ showTablePreview: showTablePreview, showTable: true });
      }
    }).catch(function (err) {
      console.error(err)
      alertUserResponse({ response: err, onConfirm: () => component.closeLoading() })
    })
  }

  runJobSelection(jobParams) {
    if (jobParams === 'RERUN_JOB') {
      alertUserV2({
        type: 'info',
        title: this.context.intl.formatMessage({ id: 'perun.batch.rerun_job_confirmation', defaultMessage: 'perun.batch.rerun_job_confirmation' }),
        confirmButtonText: this.context.intl.formatMessage({ id: 'perun.batch.confirm', defaultMessage: 'perun.batch.confirm' }),
        onConfirm: () => this.startProcess(jobParams),
        showCancel: true,
        cancelButtonText: this.context.intl.formatMessage({ id: 'perun.batch.cancel', defaultMessage: 'perun.batch.cancel' }),
      })
    } else {
      this.startProcess(jobParams)
    }
  }

  startProcess(jobParams) {
    const component = this
    const firstLabel = this.context.intl.formatMessage({ id: 'perun.batch.processing_started', defaultMessage: 'perun.batch.processing_started' })
    const secondLabel = this.context.intl.formatMessage({ id: 'perun.batch.please_wait', defaultMessage: 'perun.batch.please_wait' })
    alertUserV2({ type: 'info', title: `${firstLabel} - ` + component.state.jobId + `  ${secondLabel}` })
    let batch_params
    if (component.state.jobId) {
      batch_params = { "jobId": component.state.jobId }
    }
    const restUrl = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs[jobParams]
    axios({
      method: 'post',
      data: JSON.stringify(batch_params),
      url: restUrl + this.props.svSession,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }).then(function (response) {
      /* if job starts processing successfully, reload grid and show statistics */
      if (response?.data) {
        alertUserResponse({ response: response.data })
        component.setState({ showFormProcess: true, startJob: false, stopJob: true })
        GridManager.reloadGridData('SVAROG_BATCH_JOB' + '_' + component.state.batch_type + component.state.templateId)
        /* set interval and show statistics of the running process */
        component.showActiveProcessStats(component.state.jobId, component.interval = setInterval(
          () => component.showActiveProcessStats(component.state.jobId)
          , 7400))
      }
    }).catch(function (err) {
      console.error(err)
      alertUserResponse({ response: err })
    })
  }

  stopJob() {
    const component = this
    const restUrl = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.STOP_JOB
    let batch_params
    if (component.state.jobId) {
      batch_params = { "jobId": component.state.jobId }
    }
    axios({
      method: 'post',
      data: JSON.stringify(batch_params),
      url: restUrl,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }).then(function (response) {
      if (response?.data) {
        alertUserResponse({ response: response.data })
        component.setState({ showFormProcess: true, startJob: true, stopJob: false })
        clearInterval(component.interval)
        GridManager.reloadGridData('SVAROG_BATCH_JOB' + '_' + component.state.batch_type + component.state.templateId)
      }
    }).catch(function (err) {
      console.error(err)
      alertUserResponse({ response: err })
    })
  }

  /* create job tmp for the selected job type */
  createJsonBatchAfsardTmp(processName, processNumber, processNote, batch_type, templateId) {
    let batch_params
    let tmpId
    const component = this
    const restUrl = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.SAVE_JOB_TYPE
    if (processName && processNumber && processNote) {
      batch_params = { "name": processName, "note": processNote, "numOfThread": processNumber, "jobTypeId": batch_type, "templateId": templateId }
      tmpId = templateId
    } else {
      batch_params = { "jobTypeId": component.state.batch_type, "name": component.state.process, "note": component.state.comment, "numOfThread": component.state.num, "templateId": component.state.templateId }
      tmpId = component.state.templateId
    }
    // if form params are not undefined proceed to axios
    if (component.state.num && component.state.process) {
      axios({
        method: 'post',
        data: JSON.stringify(batch_params),
        url: restUrl + component.props.svSession,
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      }).then(function (response) {
        if (response?.data) {
          const resType = response.data?.type?.toLowerCase()
          alertUserResponse({ response: response.data })
          if (resType === 'success') {
            GridManager.reloadGridData('SVAROG_BATCH_JOB' + '_' + component.state.batch_type + component.state.templateId)
            component.getJobTemplate(tmpId)
            component.setState({ jobId: response.data.data.jobId, showJson: false })
          }
          component.setState(initialState)
        }
      }).catch(function (err) {
        console.error(err)
        alertUserResponse({ response: err })
        component.setState(initialState)
      })
    } else {
      alertUserV2({
        type: 'info',
        title: this.context.intl.formatMessage({ id: 'perun.batch.fill_in_the_form', defaultMessage: 'perun.batch.fill_in_the_form' })
      })
    }
    this.setState({ showTable: false })
  }

  getJobTemp(e, jobIdUrl) {
    let jobTemplate
    let component = this
    let jobtypeId = e
    if (jobIdUrl) {
      if (jobIdUrl !== '') {
        jobTemplate = jobIdUrl
      }
    } else {
      if (e.target) {
        if (e.target.name === 'batch_type') {
          jobtypeId = e.target.value
          component.setState({
            batchJsonTemp: '',
            showButton: false,
            [e.target.name]: e.target.value,
            showJson: false,
            showTemplate: false,
            showTable: false,
            showTablePreview: false,
            showFormProcess: false,
            startJob: false,
            stopJob: false,
            showStop: false,
            showStart: false,
            showDownloadZipBtn: false,
            showReport: false,
            showActionsBtn: false,
          })
          /* show report and actions only if "Исплати" is selected from the dropdown ? */
          // if (e.target.value == '20598576') {
          component.setState({ showReport: true, showActionsBtn: true })
          // }
        }
      }
    }

    const url = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.GET_JOB_TEMPLATES + component.props.svSession + '/' + jobtypeId
    axios.get(url).then((response) => {
      if (response.data) {
        component.iterateBatchDataTemplate(response.data, this.context.intl.formatMessage({ id: 'perun.batch.choose_process', defaultMessage: 'perun.batch.choose_process' }), 2, '', jobTemplate)
        component.inputAndSearch('', jobTemplate)
      }
    }).catch((err) => {
      console.error(err)
      alertUserResponse({ response: err })
    })

    component.setState({ showStatsState: false })
  }

  inputParams(e, getFile) {
    if (e.target.id) {
      for (let i = 0; i < this.state.formFieldId.length; i++) {
        if (e.target.id === this.state.formFieldId[i].split('-')[0]) {
          if (this.state.formFieldId[i].split('-')[1] === 'true') {
            if (e.target.value) {
              if (e.target.value.length > 0) {
                let elementArr = []
                let currentField = this.state.formFieldId[i].split('-')[0]
                let boolVal = 'false'
                elementArr.push(currentField, boolVal)
                let updatedFormFieldId = elementArr.join('-')

                let formFieldId = [...this.state.formFieldId]; // create the copy of state array
                formFieldId[i] = updatedFormFieldId; // assign new value
                this.setState({ formFieldId }); // update the state value
              }
            }
          }
        }
      }

      /* update state with uploaded file and create label with name from the file */
      if (getFile === 'getUploadName') {
        this.setState({ [e.target.id]: e.target.files[0] })
        let file = e.target.files[0]
        document.getElementById('uploadLabel').innerHTML = file.name
      } else {
        this.setState({ [e.target.id]: e.target.value })
      }
    }
  }

  inputAndSearch(e, predefValue) {
    let showButton = <div id='searchb' className='expanseBtnHolder'><button className='batch-button-default batch-button' onClick={this.showCreateForm}>{this.context.intl.formatMessage({ id: 'perun.batch.create', defaultMessage: 'perun.batch.create' })}</button><button className='batch-button-default batch-button' onClick={this.showSearchResult}>{this.context.intl.formatMessage({ id: 'perun.batch.search', defaultMessage: 'perun.batch.search' })}</button></div>
    if (predefValue) {
      this.setState({ 'templateId': predefValue, showButton: showButton })
    } else if (e) {
      this.setState({ [e.target.name]: e.target.value, showButton: showButton, showTablePreview: false, showDownloadZipBtn: false, formRemoveIDBR: false, showActionsBtn: false, showReport: false, showStatsState: true, showStatsHtml: false, showFormProcess: false, startJob: false, stopJob: false })
    }
    this.setState({ showStatsState: false })
  }

  showSearchResult(batch_type, templateId) {
    this.setState({ showTablePreview: null })
    if (this.state.templateId) {
      templateId = this.state.templateId
    }
    if (this.state.batch_type) {
      batch_type = this.state.batch_type
    }
    let showTablePreview = <SearchResults key={'SVAROG_BATCH_JOB_' + batch_type + templateId} templateId={templateId} batch_type={batch_type} getRowId={this.getRowId} batchJobParamDisplay={this.batchJobParamDisplay} />
    this.setState({ showTablePreview: showTablePreview, showTable: true, showJson: false, showTemplate: false, showActionsBtn: false, showReport: false, showFormProcess: false, showStatsState: false, showDownloadZipBtn: false, startJob: false, stopJob: false })
  }

  batchJobParamDisplay() {
    if (this.state.jobId && this.state.batchName) {
      let th1s = this
      let url = window.server + '/SvBatch/getBatchJobParam/' + th1s.props.svSession + '/' + th1s.state.jobId
      axios.get(url).then((response) => {
        if (response.data) {
          th1s.showDisplayBatchJobParamModal(response.data)
        }
      }).catch((err) => {
        console.error(err)
        alertUserResponse({ response: err })
      })
    } else {
      alertUserV2({
        type: 'info',
        title: this.context.intl.formatMessage({ id: 'perun.batch.choose_appropriate_grid_row', defaultMessage: 'perun.batch.choose_appropriate_grid_row' })
      })
    }
  }

  showDisplayBatchJobParamModal(batchJobParam) {
    if (batchJobParam) {
      let batchParamArr = []
      for (let i = 0; i < batchJobParam.length; i++) {
        let modalContent = <div style={{ textAlign: 'center' }}><span>{batchJobParam[i].name}</span> : <span style={{ fontWeight: 'bold' }}>{batchJobParam[i].value}</span></div>
        batchParamArr.push(modalContent)
      }
      let batchJobParamModal = <Modal
        modalTitle={this.state.batchName}
        modalContent={batchParamArr}
        modalSize={60}
        classBody='customModalStyle'
        closeModal={this.closeDisplayBatchJobParamModal}
      />
      this.setState({ displayBatchJobParamModal: batchJobParamModal })
    }
  }

  closeDisplayBatchJobParamModal() {
    this.setState({ displayBatchJobParamModal: false })
  }

  showCreateForm() {
    this.setState({ showTable: false, showJson: true, showFormProcess: false, showTemplate: false, showStop: false, showStart: false, showTablePreview: false, showActionsBtn: false, showReport: false, showStatsState: false, showDownloadZipBtn: false, startJob: false, stopJob: false })
  }

  onChange(e) {
    this.setState({ [e.target.name]: e.target.value })
  }

  onReset(e) {
    e.preventDefault()
    this.setState(initialState)
  }
  /* download zip report in new tab */
  downloadZip() {
    const url = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.DOWNLOAD_ZIP_FILE + this.props.svSession + '/' + this.state.jobId
    window.open(url, 'zip downloaded')
  }

  closeModal() {
    this.setState({ formRemoveIDBR: false, showJson: false, showDeleteModal: false });
    this.setState(initialState)
  }

  saveSelectedIdbr() {
    let component = this.state
    const restUrl = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.REMOVE_OBJ_FROM_SELECTION
    let batch_params = { "jobId": component.jobId, "files": component.removeIdbr }

    axios({
      method: 'post',
      data: JSON.stringify(batch_params),
      url: restUrl + this.props.svSession,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }).then(function () {
      alertUserV2({ type: 'success', title: 'Success' })
      component.setState({ showTemplate: false })
      component.selectObjects();
    }).catch(function (err) {
      console.error(err)
      alertUserResponse({ response: err })
    })
  }

  componentWillUnmount() {
    ComponentManager.setStateForComponent('SVAROG_BATCH_JOB_' + this.state.batch_type, 'rowClicked', [])
    clearInterval(this.interval)
  }

  /* get payment/process actions */
  getActions(jobId) {
    const url = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.GET_ACTIONS + this.props.svSession + '/' + jobId
    axios.get(url).then((response) => {
      if (response?.data) {
        this.iterateActions(response.data)
      }
    }).catch((err) => {
      console.error(err)
      alertUserResponse({ response: err })
    })
  }

  /* iterate payment/process actions */
  iterateActions(actions) {
    let actionsArr = []
    let actionEl
    actions.forEach(action => {
      if (!action.inputType) {
        actionEl = <button className='batch-button button-home custom-form-elements' onClick={() => this.generatePayment(action.url)}>{action.text}</button>
        actionsArr.push(actionEl)
      } else null
      if (action.inputType) {
        actionEl = <button className='batch-button button-home custom-form-elements' onClick={() => this.showModal(action.url)}>{action.text}</button>
        actionsArr.push(actionEl)
      } else null
    })
    this.setState({ showActionsBtn: actionsArr })
  }

  /* get job report */
  getReport(jobId) {
    const url = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.GET_BATCH_REPORTS + this.props.svSession + '/' + jobId
    axios.get(url).then((response) => {
      if (response.data) {
        this.iterateReport(response.data)
      }
    }).catch((err) => {
      console.error(err)
      alertUserResponse({ response: err })
    })
  }

  iterateReport(reports) {
    let reportsArr = []
    let reportsEl
    reports.forEach(report => {
      reportsEl = <button className='batch-button button-home custom-form-elements' id={report.value} onClick={() => this.showReport(report.url)}>{report.text}</button>
      reportsArr.push(reportsEl)
    })
    this.setState({ showReport: reportsArr })
  }

  showReport(param) {
    let url = param
    url = window.server + url.replace('%session', this.props.svSession).replace('%objectId', this.state.jobId)
    window.open(url, 'Report')
  }

  generatePayment(url) {
    let PAYMENT_BATCH = url
    let th1s = this
    const restUrl = config.svConfig.restSvcBaseUrl + PAYMENT_BATCH
    let batch_payment = { "jobId": th1s.state.jobId }
    th1s.setState({ loading: <Loading /> })
    axios({
      method: 'post',
      data: JSON.stringify(batch_payment),
      url: restUrl + this.props.svSession,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }).then(function (response) {
      alertUserResponse({ response: response?.data, onConfirm: () => th1s.closeLoading() })
    }).catch(function (err) {
      console.error(err)
      alertUserResponse({ response: err, onConfirm: () => th1s.closeLoading() })
    })
  }

  closeLoading() {
    this.setState({ loading: false })
  }

  showModal(paymentRemoveUrl) {
    let input = <textarea className='custom-form-elements custom-textarea' name='delete' value={this.state.delete} placeholder={this.context.intl.formatMessage({ id: 'perun.batch.enter_idn', defaultMessage: 'perun.batch.enter_idn' })} label='delete' type='input' onChange={this.onChange} id='delete'
    />
    let modal = <Modal modalTitle={this.context.intl.formatMessage({ id: 'perun.batch.delete_selection', defaultMessage: 'perun.batch.delete_selection' })} nameSubmitBtn={this.context.intl.formatMessage({ id: 'perun.batch.save_changes', defaultMessage: 'perun.batch.save_changes' })} modalContent={input} submitAction={() => this.deleteActionModal(paymentRemoveUrl)} nameCloseBtn={this.context.intl.formatMessage({ id: 'perun.batch.close', defaultMessage: 'perun.batch.close' })} closeModal={this.closeModal} />
    this.setState({ showDeleteModal: modal })
  }

  deleteActionModal(paymentRemoveUrl) {
    let REMOVE_PAYMENT_SELECTION = paymentRemoveUrl
    const th1s = this
    const restUrl = config.svConfig.restSvcBaseUrl + REMOVE_PAYMENT_SELECTION
    let delete_payment = { "jobId": th1s.state.jobId, "files": th1s.state.delete }
    axios({
      method: 'post',
      data: JSON.stringify(delete_payment),
      url: restUrl + this.props.svSession,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }).then(function (response) {
      alertUserResponse({ response: response?.data })
      th1s.setState({ showDeleteModal: false })
    }).catch(function (err) {
      console.error(err)
      alertUserResponse({ response: err })
    })
    this.setState({ delete: '' })
  }

  showActiveProcessStats(jobId, status) {
    const url = config.svConfig.restSvcBaseUrl + config.svConfig.triglavRestVerbs.GET_JOB_INFO + this.props.svSession + '/' + jobId
    axios.get(url).then((response) => {
      if (response.data) {
        this.iterateStats(response?.data?.data, status)
      }
    }).catch((err) => {
      console.error(err)
      alertUserResponse({ response: err })
    })
  }

  iterateStats(stats, _status) {
    let statsArr = []
    let timeArr = []
    let statusHTML
    let numSelObj, numProcessedObj, numProcessedObjWithErr, numSkippedObj
    for (let key in stats) {
      for (let secondKey in stats[key]) {
        if (stats[key][secondKey] || stats[key][secondKey] === 0) {
          if (key === 'numSelObj') {
            statsArr.push(key + '-' + secondKey + '-' + stats[key][secondKey])
            numSelObj = stats[key][secondKey]
          }
          if (key === 'numProcessedObj') {
            statsArr.push(key + '-' + secondKey + '-' + stats[key][secondKey])
            numProcessedObj = stats[key][secondKey]
          }
          if (key === 'numProcessedObjWithErr') {
            statsArr.push(key + '-' + secondKey + '-' + stats[key][secondKey])
            numProcessedObjWithErr = stats[key][secondKey]
          }
          if (key === 'numSkippedObj') {
            statsArr.push(key + '-' + secondKey + '-' + stats[key][secondKey])
            numSkippedObj = stats[key][secondKey]
          }
          if (key === 'finishedTime') {
            timeArr.push(key + '-' + secondKey + '-' + stats[key][secondKey])
          }
          if (key === 'totalProcessingTime') {
            timeArr.push(key + '-' + secondKey + '-' + stats[key][secondKey])
          }
        }
      }
    }

    if (statsArr.length > 0) {
      statusHTML = <Statistics key={statsArr} status={statsArr} timeStats={timeArr} />
      this.setState({ showStatsState: true, showStatsHtml: statusHTML })
      this.setState({ processWithErr: true })
      /* check if the total numSelObj is equal to the sum of other processedObj and if yes stop with stats refresh */
      if (numProcessedObj + numProcessedObjWithErr + numSkippedObj === numSelObj) {
        GridManager.reloadGridData('SVAROG_BATCH_JOB' + '_' + this.state.batch_type + this.state.templateId)
        clearInterval(this.interval)
      }
    } else {
      this.setState({ showStatsState: false, showStatsHtml: '' })
      clearInterval(this.interval)
    }
  }

  render() {
    const { showButton, cadLocationGridModal, processWithErr, process, num, comment, batchJson, batchJsonTemp, batchtemplate, showJson, showTemplate, showTablePreview, showTable, showFormProcess, selectedApps, showDownloadZipBtn, showActionsBtn, showReport, showDeleteModal, showStatsHtml, showStatsState, loading, batchName, startJob, stopJob, displayBatchJobParamModal } = this.state
    return (
      <React.Fragment>
        {loading}
        {cadLocationGridModal}
        {showDeleteModal}
        {displayBatchJobParamModal}
        <div id='holdComp' className='holdComp'>
          <div className='batch-flex-parent'>
            <div className='batch-flex-form-child'>
              <div className='batch-form-container animation-top batch-flex-column' id='custom-form'>
                {showActionsBtn}
                {showReport}
                {batchJson}
                {batchJsonTemp}
                {showButton}
              </div>
              {showFormProcess && <div className='batch-form-container animation-top form-process'>
                <p>{this.context.intl.formatMessage({ id: 'perun.batch.selected_objects_number', defaultMessage: 'perun.batch.selected_objects_number' })} - {selectedApps}</p>
                <div className='batch-flex-parent'>
                  {startJob && <button className='batch-button button-create button-create-process custom-form-elements' id='startp-btn' type='submit' onClick={() => this.runJobSelection('RUN_JOB')}>{this.context.intl.formatMessage({ id: 'perun.batch.start_process', defaultMessage: 'perun.batch.start_process' })}</button>}
                  {stopJob && <button className='batch-button button-reset button-stop-process custom-form-elements' id='stop-process-btn' type='submit' onClick={this.stopJob}>{this.context.intl.formatMessage({ id: 'perun.batch.stop_process', defaultMessage: 'perun.batch.stop_process' })}</button>}
                </div>
              </div>}
              {showStatsState && <div className='batch-form-container animation-top'>
                <p>{this.context.intl.formatMessage({ id: 'perun.batch.process_stats', defaultMessage: 'perun.batch.process_stats' })} <b>{batchName}</b></p>
                {showStatsHtml}
                {processWithErr && <button className='batch-button-default button-process' onClick={() => this.runJobSelection('RERUN_JOB')}>{this.context.intl.formatMessage({ id: 'perun.batch.reprocess', defaultMessage: 'perun.batch.reprocess' })}</button>}
              </div>}
              {showDownloadZipBtn && <button title={this.context.intl.formatMessage({ id: 'perun.batch.download_log', defaultMessage: 'perun.batch.download_log' })} onClick={this.downloadZip} className='batch-button button-home custom-form-elements'>{iconManager.getIcon('download')} {this.context.intl.formatMessage({ id: 'perun.batch.system_preview', defaultMessage: 'perun.batch.system_preview' })}</button>}
            </div>
            {showJson && (
              <ReactBootstrapModal className={"farm-registry-modal"} show={showJson} onHide={() => this.setState({ showJson: false })}>
                <ReactBootstrapModal.Header className={"farm-registry-modal-header"} closeButton>
                  <ReactBootstrapModal.Title>{this.context.intl.formatMessage({ id: 'perun.batch.create_new_process', defaultMessage: 'perun.batch.create_new_process' })}</ReactBootstrapModal.Title>
                </ReactBootstrapModal.Header>
                <ReactBootstrapModal.Body className={"farm-registry-modal-body"}>
                  <div className='flex-table-child batch-form-container animation-top' id='custom-form'>
                    <InputElement name='process' value={process} placeholder={this.context.intl.formatMessage({ id: 'perun.batch.process_name', defaultMessage: 'perun.batch.process_name' })} label='process' type='input' onChange={this.onChange} id='process' />
                    <InputElement name='num' value={num} placeholder={this.context.intl.formatMessage({ id: 'perun.batch.number_threads', defaultMessage: 'perun.batch.number_threads' })} label='num' type='number' onChange={this.onChange} id='num' />
                    <textarea className='custom-form-elements custom-textarea' name='comment' value={comment} id='comment' rows='4' placeholder={this.context.intl.formatMessage({ id: 'perun.batch.notes', defaultMessage: 'perun.batch.notes' })} onChange={this.onChange} />
                    <div className='button-container'>
                      <button className='batch-button-default' id='reset-btn' onClick={this.onReset}>{this.context.intl.formatMessage({ id: 'perun.batch.reset', defaultMessage: 'perun.batch.reset' })}</button>
                      <button className='batch-button-default' id='save-btn' type='submit' onClick={this.createJsonBatchAfsardTmp}>{this.context.intl.formatMessage({ id: 'perun.batch.create_process', defaultMessage: 'perun.batch.create_process' })}</button>
                    </div>
                  </div>
                </ReactBootstrapModal.Body>
                <ReactBootstrapModal.Footer className={"farm-registry-modal-footer"} />
              </ReactBootstrapModal>
            )}
            {/***** righ side of the main screen *****/}
            {showTemplate && <div className='batch-form-container flex-table-child animation-top'>{this.context.intl.formatMessage({ id: 'perun.batch.fill_form', defaultMessage: 'perun.batch.fill_form' })} {batchtemplate}</div>}
            {showTable && <div className='flex-table-child'>{showTablePreview}</div>}
          </div>
        </div>
      </React.Fragment>
    )
  }
}

const mapStateToProps = state => ({
  svSession: state.security.svSession
})

BatchWrapper.contextTypes = {
  intl: PropTypes.object.isRequired
}

export default connect(mapStateToProps)(BatchWrapper)
