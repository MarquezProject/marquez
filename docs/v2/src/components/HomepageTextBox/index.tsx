import styles from "@site/src/components/HomepageTextBox/styles.module.css";
import React from "react";
import clsx from "clsx";

export default function HomepageTextBox(): JSX.Element {
    return (
        <section className={clsx(styles.container, 'dashed')}>
            <div className="container">
                <div>
                    <h1 className="text--center">What is Marquez?</h1>
                    <p className={clsx("text--center", styles.constrain)}>Marquez is an open source metadata service. It maintains data provenance, shows how datasets are consumed and produced, provides global visibility into job runtimes, centralizes dataset lifecycle management, and much more.</p>
                    <h5 className="text--center">Marquez was released and open sourced by <a target={"_blank"} href={"https://wework.com"}>WeWork</a>.</h5>
                </div>
            </div>
        </section>
    );
}