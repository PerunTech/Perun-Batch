import { React, connect, GridManager, ComponentManager } from 'perun-core'

class Statistics extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      statsState: ''
    }
  }

  componentDidMount() {
    let { status, timeStats } = this.props;

    if (timeStats) {
      let timeStatsArr = []
      let timeStatusHTML
      for (let i = 0; i < timeStats.length; i++) {

        const timeWrapper = {
          margin: '1rem'
        }

        timeStatusHTML = <div style={timeWrapper} key={i}>
          <div>{timeStats[i].split('-')[1]}</div>
          <div>{timeStats[i].split('-')[2]}</div>
        </div>
        timeStatsArr.push(timeStatusHTML)
      }
      this.setState({ timeStatsState: timeStatsArr })
    }

    if (status) {
      let statsArr = []
      let labelCode, statsHTML, labelValue, numSelected, bgcolor
      let dynamicWidth, maxVal
      let firstNum = status[1].split('-')[2]
      for (let i = 0; i < status.length; i++) {
        if (!firstNum) {
          dynamicWidth = 0
        }
        if (status[i].split('-')[0] == 'numSelObj') {
          maxVal = ((status[i].split('-')[2] / status[i].split('-')[2]) * 100)
          labelCode = status[i].split('-')[1]
          labelValue = status[i].split('-')[2]
          dynamicWidth = maxVal
          numSelected = labelValue
          bgcolor = 'rgba(38, 48, 56, 0.53)'
        } else {
          dynamicWidth = (((status[i].split('-')[2]) / numSelected) * 100)
          labelCode = status[i].split('-')[1]
          labelValue = status[i].split('-')[2]

          if (status[i].split('-')[0] == 'numProcessedObj') {
            bgcolor = 'rgba(26, 170, 85, 0.75)'
          }
          if (status[i].split('-')[0] == 'numProcessedObjWithErr') {
            bgcolor = 'rgb(220, 53, 69)'
          }
          if (status[i].split('-')[0] == 'numSkippedObj') {
            bgcolor = 'rgba(253, 126, 20, 0.78)'
          }
        }
        if (!status[i].split('-')[2] || status[i].split('-')[2] == 0) {
          dynamicWidth = 0
        }

        const containerStyles = {
          backgroundColor: "#e0e0de",
          margin: '0.3rem 1rem'
        }
        const fillerStyles = {
          width: `${dynamicWidth}%`,
          backgroundColor: `${bgcolor}`,
          display: 'flex',
          alignItems: 'center'
        }
        const labelStyles = {
          padding: 5,
          color: 'rgb(33, 33, 33)',
          fontWeight: 'bold',
          display: 'flex',
          justifyContent: 'space-between'
        }

        const inlineLabels = {
          clear: 'both',
          display: 'inline-block',
          overflow: 'hidden',
          whiteSpace: 'nowrap',
        }

        statsHTML = <div key={numSelected + dynamicWidth + maxVal} style={containerStyles}>
          <div className='progress-bar-striped progress-bar-animated' style={fillerStyles}>
            <span style={labelStyles}>
              <div style={inlineLabels}>{labelCode} - {labelValue}</div>
            </span>
          </div>
        </div>

        statsArr.push(statsHTML)
      }
      this.setState({ statsState: statsArr })
    }
  }

  render() {
    const { statsState, timeStatsState } = this.state
    return (
      <div>
        {statsState}
        {timeStatsState}
      </div>
    )
  }
}

export default Statistics;