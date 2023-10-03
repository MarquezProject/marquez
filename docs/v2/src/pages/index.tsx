import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import styles from './index.module.css';
import HomepageTextBox from "@site/src/components/HomepageTextBox";

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero', 'dashed', styles.heroBanner)}>
            <div className="container">
                <div className={styles.flexContainer}>
                    <div className={styles.imageContainer}>
                        <img className={styles.image} src="img/screenshot.svg" alt="Lineage Diagram"/>
                    </div>
                    <div>
                        <h1 className="hero__title">{siteConfig.title}</h1>
                        <p className="hero__subtitle">{siteConfig.tagline}</p>
                        <div className={styles.buttons}>
                            <Link
                                className="button button--secondary button--md"
                                href="https://github.com/MarquezProject/marquez">
                                Quickstart
                            </Link>
                            <Link
                                className="button button--secondary button--md margin-left--md"
                                href="https://github.com/MarquezProject/marquez">
                                GitHub
                            </Link>
                            <Link
                                className="button button--secondary button--md margin-left--md"
                                href="https://bit.ly/Marquez_invite">
                                Slack
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    );
}

export default function Home(): JSX.Element {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout
            title={`${siteConfig.title}`}
            description="Data lineage for every pipeline.">
            <HomepageHeader/>
            <HomepageTextBox/>
            <main>
                <HomepageFeatures/>
            </main>
        </Layout>
    );
}
