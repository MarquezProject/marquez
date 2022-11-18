import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

const i18next = require("i18next");

i18next
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        // lng: i18next.options.lng, // if you're using a language detector, do not define the lng option
        debug: true,
        fallbackLng: 'en',
        resources: {
            en: {
                translation: {
                    header: {
                        docs_link: 'API Docs'
                    },
                    jobs: {
                        latest_tab: 'LATEST RUN',
                        history_tab: 'RUN HISTORY',
                        location: 'LOCATION',
                        empty_title: 'No Run Information Available',
                        empty_body: 'Try adding some runs for this job.',
                        runinfo_subhead: 'FACETS',
                        runs_subhead: 'FACETS'
                    },
                    search: {
                        search: 'Search',
                        jobs: 'Jobs',
                        and: 'and',
                        datasets: 'Datasets'
                    },
                    lineage: {
                        empty_title: 'No node selected',
                        empty_body: 'Try selecting a node through search or the jobs or datasets page.'
                    },
                    sidenav: {
                          jobs: 'JOBS',
                          datasets: 'DATASETS'
                    }
                }
            },
            fr: {
                translation: {
                    header: {
                       docs_link: 'API Docs'
                    },
                    jobs: {
                        latest_tab: 'DERNIÈRE COURSE',
                        history_tab: "HISTORIQUE D'EXECUTION",
                        location: 'EMPLACEMENT',
                        empty_title: 'Pas de Course les Informations Disponibles',
                        empty_body: "Essayez d'ajouter quelques exécutions pour ce travail.",
                        runinfo_subhead: 'FACETTES',
                        runs_subhead: 'FACETTES',
                    },
                    search: {
                        search: 'Recherche',
                        jobs: "d'Emplois",
                        and: 'et',
                        datasets: "Jeux de Données"
                    },
                    lineage: {
                        empty_title: 'Aucun nœud sélectionné',
                        empty_body: 'Essayez de sélectionner un nœud via la recherche ou la page des travaux ou des jeux de données.'
                    },
                    sidenav: {
                        jobs: 'EMPLOIS',
                        datasets: 'JEUX DE DONNÉES'
                    }
                }
            },
            es: {
                translation: {
                    header: {
                        docs_link: 'API Docs'
                    },
                    jobs: {
                        latest_tab: 'ÚLTIMA EJECUCIÓN',
                        history_tab: 'HISTORIAL DE EJECUCIONES',
                        location: 'UBICACIÓN',
                        empty_title: 'No hay Información de Ejecución Disponible',
                        empty_body: 'Intente agregar algunas ejecuciones para este trabajo.',
                        runinfo_subhead: 'FACETAS',
                        runs_subhead: 'FACETAS'
                    },
                    search: {
                        search: 'Buscar',
                        jobs: 'Trabajos',
                        and: 'y',
                        datasets: 'Conjuntos de Datos'
                    },
                    lineage: {
                        empty_title: 'Ningún nodo seleccionado',
                        empty_body: 'Intente seleccionar un nodo mediante la búsqueda o la página de trabajos o conjuntos de datos.'
                    },
                    sidenav: {
                        jobs: 'TRABAJOS',
                        datasets: 'CONJUNTOS DE DATOS'
                    }
                }
            },
            pl: {
                translation: {
                    header: {
                        docs_link: 'Dokumentacja API'
                    },
                    jobs: {
                        latest_tab: 'OSTATNI WYKONANIE',
                        history_tab: 'HISTORIA WYKONAŃ',
                        location: 'LOKALIZACJA',
                        empty_title: 'Brak dostępnych informacji o wykonaniu',
                        empty_body: 'Spróbuj dodać kilka przebiegów dla tego zadania.',
                        runinfo_subhead: 'ASPECTY',
                        runs_subhead: 'ASPECTY'
                    },
                    search: {
                        search: 'Wyszukiwanie',
                        jobs: 'Zadania',
                        and: 'i',
                        datasets: 'Zbiory Danych'
                    },
                    lineage: {
                        empty_title: 'Nie wybrano węzła',
                        empty_body: 'Spróbuj wybrać węzeł za pomocą wyszukiwania lub strony zadań lub zestawów danych.'
                    },
                    sidenav: {
                        jobs: 'ZADANIA',
                        datasets: 'ZBIORY DANYCH'
                    }
                }
            }
        }
    });
