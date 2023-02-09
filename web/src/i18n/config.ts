// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'

const i18next = require('i18next')
export const defaultNS = 'translation'

const DETECTION_OPTIONS = {
  order: ['localStorage'],
  lookupLocalStorage: 'lng',
  caches: ['localStorage']
}

i18next
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    debug: false,
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
            runs_subhead: 'FACETS',
            dialog_delete: 'DELETE',
            dialog_confirmation_title: 'Are you sure?'
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
            datasets: 'DATASETS',
            events: 'EVENTS'
          },
          namespace_select: {
            prompt: 'ns'
          },
          dataset_info: {
            empty_title: 'No Fields',
            empty_body: 'Try adding dataset fields.',
            facets_subhead: 'FACETS',
            run_subhead: 'Created by Run',
            duration: 'Duration'
          },
          datasets: {
            latest_tab: 'LATEST SCHEMA',
            history_tab: 'VERSION HISTORY',
            column_lineage_tab: 'COLUMN LINEAGE',
            dialog_delete: 'DELETE',
            dialog_confirmation_title: 'Are you sure?'
          },
          datasets_route: {
            empty_title: 'No datasets found',
            empty_body: 'Try changing namespaces or consulting our documentation to add datasets.',
            heading: 'DATASETS',
            name_col: 'NAME',
            namespace_col: 'NAMESPACE',
            source_col: 'SOURCE',
            updated_col: 'UPDATED AT',
            status_col: 'STATUS'
          },
          datasets_column_lineage: {
            empty_title: 'No column lineage',
            empty_body: 'Column lineage not available for the specified dataset.'
          },
          jobs_route: {
            empty_title: 'No jobs found',
            empty_body: 'Try changing namespaces or consulting our documentation to add jobs.',
            heading: 'JOBS',
            name_col: 'NAME',
            namespace_col: 'NAMESPACE',
            updated_col: 'UPDATED AT',
            latest_run_col: 'LATEST RUN DURATION',
            latest_run_state_col: 'LATEST RUN STATE'
          },
          runs_columns: {
            id: 'ID',
            state: 'STATE',
            created_at: 'CREATED AT',
            started_at: 'STARTED AT',
            ended_at: 'ENDED AT',
            duration: 'DURATION'
          },
          dataset_info_columns: {
            name: 'NAME',
            type: 'TYPE',
            description: 'DESCRIPTION'
          },
          dataset_versions_columns: {
            version: 'VERSION',
            created_at: 'CREATED AT',
            fields: 'FIELDS',
            created_by_run: 'CREATED BY RUN',
            lifecycle_state: 'LIFECYCLE STATE'
          },
          events_route: {
            title: 'EVENTS',
            from_date: 'From date',
            to_date: 'To date',
            previous_page: 'Previous page',
            next_page: 'Next page',
            empty_title: 'No events found',
            empty_body: 'Try changing dates or consulting our documentation to add events.'
          },
          events_columns: {
            id: 'ID',
            state: 'STATE',
            name: 'NAME',
            namespace: 'NAMESPACE',
            time: 'TIME'
          }
        }
      },
      fr: {
        translation: {
          header: {
            docs_link: 'Documents API'
          },
          jobs: {
            latest_tab: 'DERNIÈRE COURSE',
            history_tab: "HISTORIQUE D'EXECUTION",
            location: 'EMPLACEMENT',
            empty_title: 'Pas de Course les Informations Disponibles',
            empty_body: "Essayez d'ajouter quelques exécutions pour ce travail.",
            runinfo_subhead: 'FACETTES',
            runs_subhead: 'FACETTES',
            dialog_delete: 'EFFACER',
            dialog_confirmation_title: 'Êtes-vous sûr?'
          },
          search: {
            search: 'Recherche',
            jobs: "d'Emplois",
            and: 'et',
            datasets: 'Jeux de Données'
          },
          lineage: {
            empty_title: 'Aucun nœud sélectionné',
            empty_body:
              'Essayez de sélectionner un nœud via la recherche ou la page des travaux ou des jeux de données.'
          },
          sidenav: {
            jobs: 'EMPLOIS',
            datasets: 'JEUX DE DONNÉES',
            events: 'ÉVÉNEMENTS'
          },
          namespace_select: {
            prompt: 'en'
          },
          dataset_info: {
            empty_title: 'Aucun jeu de données trouvé',
            empty_body: "Essayez d'ajouter des champs de jeu de données.",
            facets_subhead: 'FACETTES',
            run_subhead: 'Créé par Run',
            duration: 'Durée'
          },
          datasets: {
            latest_tab: 'DERNIER SCHEMA',
            history_tab: 'HISTORIQUE DES VERSIONS',
            column_lineage_tab: 'LIGNÉE DE COLONNE',
            dialog_delete: 'EFFACER',
            dialog_confirmation_title: 'Êtes-vous sûr?'
          },
          datasets_route: {
            empty_title: 'Aucun jeu de données trouvé',
            empty_body:
              'Essayez de modifier les espaces de noms ou consultez notre documentation pour ajouter des ensembles de données.',
            heading: 'JEUX DE DONNÉES',
            name_col: 'NOM',
            namespace_col: 'ESPACE DE NOMS',
            source_col: 'SOURCE',
            updated_col: 'MISE À JOUR À',
            status_col: 'STATUT'
          },
          datasets_column_lineage: {
            empty_title: 'Aucune lignée de colonne',
            empty_body: "Lignage de colonne non disponible pour l'ensemble de données spécifié."
          },
          jobs_route: {
            empty_title: 'Aucun emploi trouvé',
            empty_body:
              'Essayez de modifier les espaces de noms ou consultez notre documentation pour ajouter des travaux.',
            heading: 'EMPLOIS',
            name_col: 'NOM',
            namespace_col: 'ESPACE DE NOMS',
            updated_col: 'MISE À JOUR À',
            latest_run_col: "DERNIÈRE DURÉE D'EXÉCUTION",
            latest_run_state_col: "DERNIER ÉTAT D'EXÉCUTIONE"
          },
          runs_columns: {
            id: 'ID',
            state: 'ETAT',
            created_at: 'CRÉÉ À',
            started_at: 'COMMENCÉ À',
            ended_at: 'TERMINÉ À',
            duration: 'DURÉE'
          },
          dataset_info_columns: {
            name: 'NOM',
            type: 'TAPER',
            description: 'DESCRIPTION'
          },
          dataset_versions_columns: {
            version: 'VERSION',
            created_at: 'CRÉÉ À',
            fields: 'DOMAINES',
            created_by_run: 'CRÉÉ PAR RUN',
            lifecycle_state: 'ÉTAT DU CYCLE DE VIE'
          },
          events_route: {
            title: 'ÉVÉNEMENTS',
            from_date: 'Partir de la date',
            to_date: 'À la date',
            previous_page: 'Page précédente',
            next_page: 'Page suivante',
            empty_title: 'Aucun événement trouvé',
            empty_body:
              'Essayez de changer les dates ou consultez notre documentation pour ajouter des événements.'
          },
          events_columns: {
            id: 'ID',
            state: 'ETAT',
            name: 'NOM',
            namespace: 'ESPACE DE NOMS',
            time: 'TEMPS'
          }
        }
      },
      es: {
        translation: {
          header: {
            docs_link: 'Documentos API'
          },
          jobs: {
            latest_tab: 'ÚLTIMA EJECUCIÓN',
            history_tab: 'HISTORIAL DE EJECUCIONES',
            location: 'UBICACIÓN',
            empty_title: 'No hay Información de Ejecución Disponible',
            empty_body: 'Intente agregar algunas ejecuciones para este trabajo.',
            runinfo_subhead: 'FACETAS',
            runs_subhead: 'FACETAS',
            dialog_delete: 'ELIMINAR',
            dialog_confirmation_title: 'Estás seguro?'
          },
          search: {
            search: 'Buscar',
            jobs: 'Trabajos',
            and: 'y',
            datasets: 'Conjuntos de Datos'
          },
          lineage: {
            empty_title: 'Ningún nodo seleccionado',
            empty_body:
              'Intente seleccionar un nodo mediante la búsqueda o la página de trabajos o conjuntos de datos.'
          },
          sidenav: {
            jobs: 'TRABAJOS',
            datasets: 'CONJUNTOS DE DATOS',
            events: 'EVENTOS'
          },
          namespace_select: {
            prompt: 'en'
          },
          dataset_info: {
            empty_title: 'No se encontraron conjuntos de datos',
            empty_body: 'Intente agregar campos de conjuntos de datos.',
            facets_subhead: 'FACETAS',
            run_subhead: 'Creado por Ejecutar',
            duration: 'Duración'
          },
          datasets: {
            latest_tab: 'ESQUEMA ÚLTIMO',
            history_tab: 'HISTORIAL DE VERSIONES',
            column_lineage_tab: 'LINAJE DE COLUMNA',
            dialog_delete: 'ELIMINAR',
            dialog_confirmation_title: 'Estás seguro?'
          },
          datasets_route: {
            empty_title: 'No se encontraron conjuntos de datos',
            empty_body:
              'Intente cambiar los espacios de nombres o consultar nuestra documentación para agregar conjuntos de datos.',
            heading: 'CONJUNTOS DE DATOS',
            name_col: 'NOMBRE',
            namespace_col: 'ESPACIO DE NOMBRES',
            source_col: 'FUENTE',
            updated_col: 'ACTUALIZADO EN',
            status_col: 'ESTADO'
          },
          datasets_column_lineage: {
            empty_title: 'Sin linaje de columna',
            empty_body: 'Linaje de columna no disponible para el conjunto de datos especificado.'
          },
          jobs_route: {
            empty_title: 'No se encontraron trabajos',
            empty_body:
              'Intente cambiar los espacios de nombres o consultar nuestra documentación para agregar trabajos.',
            heading: 'TRABAJOS',
            name_col: 'NOMBRE',
            namespace_col: 'ESPACIO DE NOMBRES',
            updated_col: 'ACTUALIZADO EN',
            latest_run_col: 'DURACIÓN DE LA ÚLTIMA EJECUCIÓN',
            latest_run_state_col: 'ESTADO DE LA ÚLTIMA EJECUCIÓN'
          },
          runs_columns: {
            id: 'ID',
            state: 'ESTADO',
            created_at: 'CREADO EN',
            started_at: 'EMPEZÓ A LAS',
            ended_at: 'TERMINÓ EN',
            duration: 'DURACIÓN'
          },
          dataset_info_columns: {
            name: 'NOMBRE',
            type: 'ESCRIBE',
            description: 'DESCRIPCIÓN'
          },
          dataset_versions_columns: {
            version: 'VERSIÓN',
            created_at: 'CREADO EN',
            fields: 'CAMPOS',
            created_by_run: 'CREADO POR EJECUTAR',
            lifecycle_state: 'ESTADO DEL CICLO DE VIDA'
          },
          events_route: {
            title: 'EVENTOS',
            from_date: 'Partir de la fecha',
            to_date: 'Hasta la fecha',
            previous_page: 'Pagina anterior',
            next_page: 'Siguiente página',
            empty_title: 'No se encontraron eventos',
            empty_body:
              'Prueba a cambiar las fechas o consulta nuestra documentación para añadir eventos.'
          },
          events_columns: {
            id: 'ID',
            state: 'ESTADO',
            name: 'NOMBRE',
            namespace: 'ESPACIO DE NOMBRES',
            time: 'TIEMPO'
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
            runs_subhead: 'ASPECTY',
            dialog_delete: 'USUNĄĆ',
            dialog_confirmation_title: 'Jesteś pewny?'
          },
          search: {
            search: 'Wyszukiwanie',
            jobs: 'Zadania',
            and: 'i',
            datasets: 'Zbiory Danych'
          },
          lineage: {
            empty_title: 'Nie wybrano węzła',
            empty_body:
              'Spróbuj wybrać węzeł za pomocą wyszukiwania lub strony zadań lub zestawów danych.'
          },
          sidenav: {
            jobs: 'ZADANIA',
            datasets: 'ZBIORY DANYCH',
            events: 'WYDARZENIA'
          },
          namespace_select: {
            prompt: 'pn'
          },
          dataset_info: {
            empty_title: 'Nie znaleziono zbiorów danych',
            empty_body: 'Spróbuj dodać pola zbiory danych.',
            facets_subhead: 'ASPECTY',
            run_subhead: 'Stworzony przez Run',
            duration: 'Czas trwania'
          },
          datasets: {
            latest_tab: 'NAJNOWSZY SCHEMAT',
            history_tab: 'HISTORIA WERSJI',
            column_lineage_tab: 'RODOWÓD KOLUMNOWY',
            dialog_delete: 'USUNĄĆ',
            dialog_confirmation_title: 'Jesteś pewny?'
          },
          datasets_route: {
            empty_title: 'Nie znaleziono zbiorów danych',
            empty_body:
              'Spróbuj zmienić przestrzenie nazw lub zapoznaj się z naszą dokumentacją, aby dodać zbiory danych.',
            heading: 'ZBIORY DANYCH',
            name_col: 'NAZWA',
            namespace_col: 'PRZESTRZEŃ NAZW',
            source_col: 'ŹRÓDŁO',
            updated_col: 'ZAKTUALIZOWANO',
            status_col: 'STATUS'
          },
          datasets_column_lineage: {
            empty_title: 'Brak rodowodu kolumny',
            empty_body: 'Pochodzenie kolumny jest niedostępne dla określonego zbioru danych.'
          },
          jobs_route: {
            empty_title: 'Nie znaleziono ofert pracy',
            empty_body:
              'Spróbuj zmienić przestrzenie nazw lub zapoznaj się z naszą dokumentacją, aby dodać zadania.',
            heading: 'ZADANIA',
            name_col: 'NAZWA',
            namespace_col: 'PRZESTRZEŃ NAZW',
            updated_col: 'ZAKTUALIZOWANO',
            latest_run_col: 'NAJNOWSZY CZAS TRWANIA',
            latest_run_state_col: 'NAJNOWSZY STAN URUCHOMIENIA'
          },
          runs_columns: {
            id: 'ID',
            state: 'PAŃSTWO',
            created_at: 'UTWORZONY W',
            started_at: 'ROZPOCZĘŁO SIĘ O GODZ',
            ended_at: 'ZAKOŃCZONE O GODZ',
            duration: 'TRWANIE'
          },
          dataset_info_columns: {
            name: 'NAZWA',
            type: 'RODZAJ',
            description: 'OPIS'
          },
          dataset_versions_columns: {
            version: 'WERSJA',
            created_at: 'UTWORZONY W',
            fields: 'KIERUNKI',
            created_by_run: 'STWORZONY PRZEZ URUCHOM',
            lifecycle_state: 'STAN CYKLU ŻYCIA'
          },
          events_route: {
            title: 'WYDARZENIA',
            from_date: 'Od daty',
            to_date: 'Spotykać się z kimś',
            previous_page: 'Poprzednia strona',
            next_page: 'Następna strona',
            empty_title: 'Nie znaleziono wydarzeń',
            empty_body:
              'Spróbuj zmienić daty lub zapoznaj się z naszą dokumentacją, aby dodać wydarzenia.'
          },
          events_columns: {
            id: 'ID',
            state: 'PAŃSTWO',
            name: 'NAZWA',
            namespace: 'PRZESTRZEŃ NAZW',
            time: 'CZAS'
          }
        }
      }
    },
    defaultNS,
    detection: DETECTION_OPTIONS
  })
