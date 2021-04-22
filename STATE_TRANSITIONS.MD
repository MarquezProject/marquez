# Run State Transitions
Run states change based on the success or failure of a job run. The datasets consumed and/or produced
by a job run are immutable and do not change based on the success or failure of the run.

# Dataset State Transitions
Dataset states change based on the success or failure of job runs that consume or generate those 
datasets. Typically, Runs generate dataset versions as outputs, but sometimes a job run may result 
in an input dataset version being created if that dataset did not previously exist in the database.
Once a dataset exists, a new version will only be created if the dataset is the output of a job run. 

### Base case
* Given
  * JobA does not exist
  * DatasetX does not exist
  * DatasetY does not exist
* When
  * JobA@Run1 **succeeds**
  * JobA consumes DatasetX
  * JobA produces DatasetY
* Then
  * Create JobA V1
  * Create DatasetX@V1
  * Set DatasetX current version to V1
  * JobA@Run1 consumes DatasetX@V1
  * Create DatasetY@V1
  * Set DatasetY current version to V1
  * JobA@Run1 produces DatasetY@V1

### Consume existing dataset
* Given
    * JobA does not exist
    * DatasetX@V1 exists
    * DatasetY does not exist
* When
    * JobA@Run1 **succeeds**
    * JobA consumes DatasetX
    * JobA produces DatasetY
* Then
    * Create JobA@Run1
    * JobA@Run1 consumes DatasetX@V1
    * Create DatasetY@V1
    * Set DatasetY current version to V1
    * JobA@Run1 produces DatasetY@V1

### Consume and produce existing datasets
* Given
    * JobA exists
    * DatasetX@V1 exists
    * DatasetY@V1 exists
* When
    * JobA@Run2 **succeeds**
    * JobA consumes DatasetX
    * JobA produces DatasetY
* Then
    * Create JobA@Run2
    * JobA@Run1 consumes DatasetX@V1
    * Create DatasetY@V2
    * Set DatasetY current version to V2
    * JobA@Run2 produces DatasetY@V2

### Successful job chain
* Given
    * JobA does not exist
    * JobB does not exist
    * DatasetX does not exist
    * DatasetY does not exist
* When
    * JobA@Run1 **succeeds**
    * JobA produces DatasetX
    * JobB consumes DatasetX
    * JobB produces DatasetY
* Then
    * Create JobA@Run1
    * Create JobB@Run1
    * Create DatasetX@V1
    * Set DatasetX current version to V1
    * Create DatasetY@V1
    * Set DatasetY current version to V1
    * JobA@Run1 produces DatasetX@V1
    * JobB@Run1 consumes DatasetX@V1
    * JobB@Run1 produces DatasetY@V1
![Consume and produce dataset](./docs/assets/dot/job_input_dataset_creation.dot.png)

### Successful job chain with existing datasets
* Given
    * JobA@Run1 exists
    * JobB@Run1 exists
    * DatasetX@V1 exists
    * DatasetY@V1 exists
* When
    * JobA@Run1 **succeeds**
    * JobA produces DatasetX
    * JobB consumes DatasetX
    * JobB produces DatasetY
* Then
    * Create DatasetX@V2
    * Set DatasetX current version to V2
    * Create DatasetY@V2
    * Set DatasetY current version to V2
    * JobA@Run1 produces DatasetX@V2
    * JobB@Run1 consumes DatasetX@V2
    * JobB@Run1 produces DatasetY@V2

### Failed job chain with existing datasets - workflow stops
* Given
    * JobA@Run1 exists
    * JobB@Run1 exists
    * DatasetX@V1 exists
    * DatasetY@V1 exists
    * JobA@Run1 produces DatasetX@V1
    * JobB@Run1 consumes DatasetX@V1
    * JobB@Run1 produces DatasetY@V1
* When
    * JobA@Run2 **fails**
    * JobA produces DatasetX
* Then
    * Create DatasetX@V2
    * DatasetX current version is V1
    * JobA@Run1 produces DatasetX@V1
    * JobB@Run1 consumes DatasetX@V1
    * JobB@Run1 produces DatasetY@V1
    * JobA@Run2 produces DatasetX@V2
    * DatasetY current version remains at V1
![Consume and produce dataset failed job](./docs/assets/dot/job_failed_dataset_creation.dot.png)

### Failed job chain with existing datasets - workflow continues
* Given
    * JobA@Run1 exists
    * JobB@Run1 exists
    * DatasetX@V1 exists
    * DatasetY@V1 exists
    * JobA@Run1 produces DatasetX@V1
    * JobB@Run1 consumes DatasetX@V1
    * JobB@Run1 produces DatasetY@V1
* When
    * JobA@Run2 **fails**
    * JobA produces DatasetX
    * JobB@Run2 **succeeds**
    * JobB consumes DatasetX
    * JobB produces DatasetY
* Then
    * Create DatasetX@V2
    * DatasetX current version is V1
    * JobA@Run1 produces DatasetX@V1
    * JobB@Run1 consumes DatasetX@V1
    * JobB@Run1 produces DatasetY@V1
    * JobA@Run2 produces DatasetX@V2
    * JobB@Run2 consumes DatasetX@V1
    * JobB@Run2 produces DatasetY@V2

### Parent jobs with successful child jobs
Parent job success/failure does not impact the status of the datasets created by child jobs.
* Given
  * JobA@Run1 exists
  * DatasetX@V1 exists
  * DatasetY@V1 exists
  * DatasetZ@V1 exists
* When
  * JobB@Run1 reports JobA@Run1 as a parent job
  * JobB consumes DatasetX
  * JobB produces DatasetY
  * JobB@Run1 **succeeds**
  * JobA@Run1 **succeeds**
  * JobC@Run1 consumes DatasetY
  * JobC@Run1 produces DatasetZ
  * JobC@Run1 **succeeds**
* Then
  * Create DatasetY@V2
  * DatasetY current version is V2
  * JobB@Run1 consumes DatasetX@V1
  * JobB@Run1 produces DatasetY@V2
  * JobC@Run1 consumes DatasetY@V2
  * JobC@Run1 produces DatasetZ@V2

![Consume and produce dataset in parent job](./docs/assets/dot/parent_job_succeed_dataset_creation.dot.png)

### Parent jobs with failed child jobs
Parent job failure does not impact the status of the datasets created by child jobs.
* Given
  * JobA@Run1 exists
  * DatasetX@V1 exists
  * DatasetY@V1 exists
  * DatasetZ@V1 exists
* When
  * JobB@Run1 reports JobA@Run1 as a parent job
  * JobB consumes DatasetX
  * JobB produces DatasetY
  * JobB@Run1 **fails**
  * JobA@Run1 **succeeds**
  * JobC@Run1 consumes DatasetY
  * JobC@Run1 produces DatasetZ
  * JobC@Run1 **succeeds**
* Then
  * Create DatasetY@V2
  * DatasetY current version is V2
  * JobB@Run1 consumes DatasetX@V1
  * JobC@Run1 consumes DatasetY@V1
  * JobC@Run1 produces DatasetZ@V2
![Parent has failed child jobs](./docs/assets/dot/parent_job_component_failed_dataset_creation.dot.png)

### Parent jobs **fails**
Parent job failure does not impact the status of the datasets created by child jobs.
* Given
  * JobA@Run1 exists
  * DatasetX@V1 exists
  * DatasetY@V1 exists
  * DatasetZ@V1 exists
* When
  * JobB@Run1 reports JobA@Run1 as a parent job
  * JobB consumes DatasetX
  * JobB produces DatasetY
  * JobB@Run1 **succeeds**
  * JobA@Run1 **fails**
  * JobC@Run1 consumes DatasetY
  * JobC@Run1 produces DatasetZ
  * JobC@Run1 **succeeds**
* Then
  * Create DatasetY@V2
  * DatasetY current version is V2
  * JobB@Run1 consumes DatasetX@V1
  * JobB@Run1 produces DatasetY@V2
  * JobC@Run1 consumes DatasetY@V2
  * JobC@Run1 produces DatasetZ@V2
![Parent has failed child jobs](./docs/assets/dot/parent_job_failed_dataset_creation.dot.png)
