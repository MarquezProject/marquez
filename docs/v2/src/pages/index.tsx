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
            <div>
                <div className={styles.flexContainer}>
                    
                    <div className={styles.titleContainer}>
                        <img className={styles.logo} src="img/logo_txt.png"/>
                        <p className="hero__subtitle">{siteConfig.tagline}</p>
                        <div className={styles.buttons}>
                            <Link
                                className="button button--primary button--md"
                                href="https://github.com/MarquezProject/marquez">
                                Quickstart
                            </Link>
                            <Link
                                className="button button--primary button--md margin-left--md"
                                href="https://github.com/MarquezProject/marquez">
                                <img 
                                    className={styles.btn_logos} 
                                    src="img/github.svg" 
                                    alt="GitHub logo" 
                                />
                                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;GitHub
                            </Link>
                            <Link
                                className="button button--primary button--md margin-left--md"
                                href="https://bit.ly/Mqz_invite">
                                <img 
                                    className={styles.btn_logos} 
                                    src="img/slack.svg" 
                                    alt="Slack logo" 
                                />
                                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Slack
                            </Link>
                        </div>
                    </div>
                    <div className={styles.imageContainer}>
                        <img className={styles.image} src="img/marquez_caps_triplet.png" alt="Lineage Diagram"/>
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
