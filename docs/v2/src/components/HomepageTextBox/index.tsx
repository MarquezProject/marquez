import styles from "@site/src/components/HomepageTextBox/styles.module.css";
import React from "react";
import clsx from "clsx";

export default function HomepageTextBox(): JSX.Element {
    return (
        <section className={clsx(styles.container, 'dashed')}>
            <div className="container">
                <div>
                    <h1 className="text--center">One Source of Truth</h1>
                    <p className={clsx("text--center", styles.constrain)}>Marquez enables consuming, storing, and visualizing <a target={"_blank"} href={"https://openlineage.io"}>OpenLineage</a> metadata from across an organization, serving use cases including data governance, data quality monitoring, and performance analytics.</p>
                    <h5 className="text--center">Marquez was released and open sourced by <a target={"_blank"} href={"https://wework.com"}>WeWork</a>.</h5>
                </div>
            </div>
        </section>
    );
}