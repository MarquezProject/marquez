import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const featureList: FeatureItem[] = [
    {
        title: 'One Service for Lineage',
        subTitle: 'Real-time metadata server',
        imgSrc: 'img/stack.svg',
        description: <>
            <p>
                Marquez includes a metadata server offering an OpenLineage-compatible endpoint for real-time
                collection of information from running jobs and applications.
            </p>
            <p>
                As the reference implementation of
                OpenLineage, the Marquez API server already works with all the integrations developed by the
                OpenLineage community.
                This includes Apache Airflow, Apache Spark, Apache Flink, dbt, and Dagster.
            </p>
        </>,
        side: 'left',
    },
    {
        subTitle: 'Unified visual graph',
        imgSrc: 'img/unified_graph.png',
        side: 'right',
        description: <>
            <p>
                Through a web user interface, Marquez provides a visual map that shows complex interdependencies
                within your data ecosystem.
            </p>
            <p>
                The user interface allows you to browse the metadata within Marquez, making it easy to see the inputs
                and outputs of each job, trace the lineage of individual datasets, and study performance metrics and
                execution details.
            </p>
        </>
    },
    {
        subTitle: 'Flexible Lineage API',
        imgSrc: 'img/api-terminal.png',
        side: 'left',
        description: <>
            <p>
                Through a lineage API, metadata can be queried for automation of key tasks like
                backfills and root cause analysis.
            </p>
            <p>
                With the Lineage API, you can easily traverse the dependency tree and establish context for datasets
                across multiple pipelines and orchestration platforms. This can be used to enrich data catalogs and data
                quality systems.
            </p>
        </>
    }
]

type FeatureItem = {
    title?: string
    subTitle: string
    imgSrc: string
    description: React.JSX.Element
    side: 'left' | 'right'
}

function Feature({title, imgSrc, subTitle, side, description}: FeatureItem) {
    return (
        <div className={'container'}>
            {title && <h1 className={'text--center margin-top--lg margin-bottom--lg'}>{title}</h1>}
            <div className={clsx(styles.flexContainer, side === 'right' && styles.rightImage)}>
                <div className={styles.imageContainer}>
                    <img className={styles.image} src={imgSrc} alt="Marquez Image"/>
                </div>
                <div className={styles.constrain}>
                    <h3>{subTitle}</h3>
                    <p>{description}</p>
                </div>
            </div>
        </div>
    );
}

export default function HomepageFeatures(): JSX.Element {
    return (
        <section className={clsx(styles.features, 'dashed')}>
            <div className="container">
                <div className="row">
                    {featureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
