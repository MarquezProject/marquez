import React, { ChangeEvent, FunctionComponent, SetStateAction, useEffect } from 'react'

import * as Redux from 'redux'
import { Box, Chip, Tab, Tabs } from '@material-ui/core'
import { DatasetVersion } from '../../types/api'
import { IState } from '../../store/reducers'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { LineageDataset } from '../lineage/types'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchDatasetVersions } from '../../store/actionCreators'
import { useHistory, useParams } from 'react-router-dom'
import CloseIcon from '@material-ui/icons/Close'
import DatasetInfo from './DatasetInfo'
import DatasetVersions from './DatasetVersions'
import IconButton from '@material-ui/core/IconButton'
import MqText from '../core/text/MqText'

const styles = ({ spacing }: ITheme) => {
  return createStyles({
    root: {
      padding: `0 ${spacing(2)}px`
    },
    tagList: {
      display: 'flex',
      flexWrap: 'wrap',
      listStyle: 'none',
      margin: 0,
      padding: 0
    },
    tag: {
      '&:not(:last-of-type)': {
        marginRight: spacing(1)
      }
    },
    noData: {
      padding: '125px 0 0 0'
    },
    infoIcon: {
      paddingLeft: '3px',
      paddingTop: '3px'
    },
    updated: {
      marginTop: '10px'
    }
  })
}

interface StateProps {
  dataset: LineageDataset
  versions: DatasetVersion[]
}

interface DispatchProps {
  fetchDatasetVersions: typeof fetchDatasetVersions
}

type IProps = IWithStyles<typeof styles> & StateProps & DispatchProps

function a11yProps(index: number) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`
  }
}

const DatasetDetailPage: FunctionComponent<IProps> = props => {
  const { dataset, classes, fetchDatasetVersions } = props
  const { root } = classes
  const { datasetName } = useParams()
  const history = useHistory()

  useEffect(() => {
    fetchDatasetVersions(props.dataset.namespace, props.dataset.name)
  }, [props.dataset.name])

  const [value, setValue] = React.useState(0)
  const handleChange = (event: ChangeEvent, newValue: SetStateAction<number>) => {
    setValue(newValue)
  }

  if (!dataset) {
    return (
      <Box display='flex' justifyContent='center' className={root} mt={2}>
        <MqText subdued>
          No dataset by the name of <MqText bold inline>{`"${datasetName}"`}</MqText> found
        </MqText>
      </Box>
    )
  } else {
    const { name, description, tags } = dataset
    return (
      <Box my={2} className={root}>
        <Box>
          {tags.length > 0 && (
            <ul className={classes.tagList}>
              {tags.map(tag => (
                <li key={tag.name} className={classes.tag}>
                  <Chip size='small' label={tag} />
                </li>
              ))}
            </ul>
          )}
          <Box display={'flex'} justifyContent={'space-between'} mb={2}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
              <Tabs
                value={value}
                onChange={handleChange}
                textColor='primary'
                indicatorColor='primary'
              >
                <Tab label='Current' {...a11yProps(0)} disableRipple={true} />
                <Tab label='Versions' {...a11yProps(1)} disableRipple={true} />
              </Tabs>
            </Box>
            <IconButton onClick={() => history.push('/')}>
              <CloseIcon />
            </IconButton>
          </Box>
          <MqText heading font={'mono'}>
            {name}
          </MqText>
          <Box mb={2}>
            <MqText subdued>{description}</MqText>
          </Box>
        </Box>
        {value === 0 && <DatasetInfo datasetFields={dataset.fields} facets={dataset.facets} />}
        {value === 1 && <DatasetVersions versions={props.versions} />}
      </Box>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets.result,
  versions: state.datasetVersions.result.versions
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDatasetVersions: fetchDatasetVersions
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(DatasetDetailPage))
