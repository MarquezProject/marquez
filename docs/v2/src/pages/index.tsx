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
            <div className={styles.titleContainer}>
                <img className={styles.logo} src="img/logo_txt_grn.svg"/>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <div className={styles.buttons}>
                    <Link
                        className="button button--secondary button--md"
                        href="docs/quickstart">
                        Quickstart
                    </Link>
                    <Link
                        className="button button--secondary button--md margin-left--md"
                        href="https://github.com/MarquezProject/marquez">
                        <img 
                            className={styles.btn_logos} 
                            src="img/github.svg" 
                            alt="GitHub logo" 
                        />
                        <span className={styles.btn_text}>GitHub</span>
                    </Link>
                    <Link
                        className="button button--secondary button--md margin-left--md"
                        href="https://bit.ly/Marquez_Slack_invite">
                        <img 
                            className={styles.btn_logos} 
                            src="img/slack.svg" 
                            alt="Slack logo" 
                        />
                        <span className={styles.btn_text}>Slack</span>
                    </Link>
                </div>
            </div>
            <div className={styles.imageSubDiv}>
                <img 
                    className={styles.image} 
                    src="img/marquez_caps_couple.svg" 
                    alt="Marquez UI Screencaps"
                />
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
