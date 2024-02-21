// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Marquez Project',
    tagline: 'Data lineage for every pipeline.',
    favicon: 'img/favicon.ico',

    // Set the production url of your site here
    url: 'https://peppy-sprite-186812.netlify.app',
    // Set the /<baseUrl>/ pathname under which your site is served
    // For GitHub pages deployment, it is often '/<projectName>/'
    baseUrl: '/',

    // GitHub pages deployment config.
    // If you aren't using GitHub pages, you don't need these.
    organizationName: 'MarquezProject', // Usually your GitHub org/user name.
    projectName: 'marquez', // Usually your repo name.

    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',

    // Even if you don't use internalization, you can use this field to set useful
    // metadata like html lang. For example, if your site is Chinese, you may want
    // to replace "en" with "zh-Hans".
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },
    
    headTags: [
        {
            tagName: 'link',
            attributes: {
                rel: 'stylesheet',
                href: 'https://fonts.googleapis.com/css?family=Karla:400,700|Source+Code+Pro:400,700&display=swap'
            }
        }
    ],
    
    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    sidebarPath: require.resolve("./sidebars.js"),
                    // Please change this to your repo.
                    // Remove this to remove the "edit this page" links.
                    editUrl:
                        "https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/",
                    docLayoutComponent: "@theme/DocPage",
                    docItemComponent: "@theme/ApiItem" // Derived from docusaurus-theme-openapi-docs
                },
                blog: {
                    blogTitle: 'Marquez Blog',
                    blogDescription: 'News and Updates on the Marquez Project',
                    showReadingTime: true,
                    postsPerPage: 'ALL',
                    // To do: update with correct url
                    editUrl:
                        'https://github.com/MarquezProject/marquez/tree/docs/v2/docs-v2/',
                },
                pages: {
                    path: 'src/pages',
                    include: ['**/*.{js,jsx,ts,tsx,md,mdx}'],
                    exclude: [
                      'home.tsx', // this page served from plugin
                      '**/_*.{js,jsx,ts,tsx,md,mdx}',
                      '**/_*.{js,jsx,ts,tsx,md,mdx}',
                      '**/_*/**',
                      '**/*.test.{js,jsx,ts,tsx}',
                      '**/__tests__/**',
                    ],
                    mdxPageComponent: '@theme/MDXPage',
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            metadata: [{property: 'og:title', content: 'Marquez Project | LFAI & Data'}],
            colorMode: {
                defaultMode: 'dark',
                disableSwitch: true
            },
            // Replace with your project's social card
            image: 'img/marquez-social-card.jpg',
            navbar: {
                logo: {
                    alt: 'My Site Logo',
                    src: 'img/logo-lfai.svg',
                },
                items: [
                    {to: '/about', label: 'About', position: 'left'},
                    {
                        type: 'docSidebar',
                        sidebarId: 'tutorialSidebar',
                        position: 'left',
                        label: 'Docs',
                    },
                    {to: '/blog', label: 'Blog', position: 'left'},
                    {to: '/resources', label: 'Resources', position: 'left'},
                    {
                        href: 'https://github.com/MarquezProject/marquez',
                        label: 'GitHub',
                        position: 'right',
                    },
                ],
            },
            footer: {
                style: 'dark',
                links: [
                    {
                        title: 'Get Started',
                        items: [
                            {
                                label: 'Quickstart',
                                to: '/docs/quickstart',
                            },
                        ],
                    },
                    {
                        title: 'Connect',
                        items: [
                            {
                                label: 'Slack',
                                href: 'https://bit.ly/Mqz_invite',
                            },
                            {
                                label: 'YouTube',
                                href: 'https://bit.ly/MarquezYouTube',
                            },
                            {
                                label: 'Twitter/X',
                                href: 'https://twitter.com/marquezproject',
                            },
                        ],
                    },
                    {
                        title: 'Contribute',
                        items: [
                            {
                                label: 'GitHub',
                                href: 'https://github.com/MarquezProject/marquez',
                            },
                        ],
                    },
                ],
                copyright: `Copyright © ${new Date().getFullYear()} Marquez Project.`,
            },
            prism: {
                darkTheme: darkCodeTheme,
                theme: lightCodeTheme,
            },
        }),
    
    scripts: [
        'js/google_analytics.js',
        {
            src: 'https://www.googletagmanager.com/gtag/js?id=G-223DXLT48B',
            async: true,
        },
    ],
    
    plugins: [
        [
            'docusaurus-plugin-openapi-docs',
            {
                id: "apiDocs",
                docsPluginId: "classic",
                config: {
                    burgers: {
                        specPath: "openapi.yml",
                        outputDir: "docs/api",
                    }
                }
            },
        ]
    ],
    
    themes: ["docusaurus-theme-openapi-docs"]
};

module.exports = config;
