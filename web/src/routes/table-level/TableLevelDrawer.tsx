import * as Redux from 'redux';
import { Box } from '@mui/system';
import { IState } from '../../store/reducers';
import { LineageDataset, LineageJob } from '../../types/lineage';
import { LineageGraph } from '../../types/api';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { useSearchParams } from 'react-router-dom';
import { trackEvent } from '../../components/ga4';
import DatasetDetailPage from '../../components/datasets/DatasetDetailPage';
import JobDetailPage from '../../components/jobs/JobDetailPage';
import React, { useEffect } from 'react';

const WIDTH = 800;

interface StateProps {
  lineageGraph: LineageGraph;
}
interface DispatchProps {}
const TableLevelDrawer = ({ lineageGraph }: StateProps & DispatchProps) => {
  const [searchParams] = useSearchParams();

  const node = lineageGraph.graph.find(
    (node) => node.id === searchParams.get('tableLevelNode') || ''
  );

  let dataset: LineageDataset | null = null;
  let job: LineageJob | null = null;
  if (node?.type === 'DATASET') {
    dataset = node.data as LineageDataset;
  } else if (node?.type === 'JOB') {
    job = node.data as LineageJob;
  }

  useEffect(() => {
    trackEvent('TableLevelDrawer', 'View Table-Level Drawer');
  }, []);

  useEffect(() => {
    if (dataset) {
      trackEvent('TableLevelDrawer', 'View Dataset Detail', dataset.name);
    } else if (job) {
      trackEvent('TableLevelDrawer', 'View Job Detail', job.name);
    }
  }, [dataset, job]);

  return (
    <Box width={`${WIDTH}px`}>
      {dataset ? (
        <DatasetDetailPage lineageDataset={dataset} />
      ) : (
        <>{job && <JobDetailPage lineageJob={job} />}</>
      )}
    </Box>
  );
};

const mapStateToProps = (state: IState) => ({
  lineageGraph: state.lineage.lineage,
});

const mapDispatchToProps = (dispatch: Redux.Dispatch) => bindActionCreators({}, dispatch);
export default connect(mapStateToProps, mapDispatchToProps)(TableLevelDrawer);