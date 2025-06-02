<g:applyLayout name="main">
    <html>
    <head>
        <title>
            <g:layoutTitle/>
        </title>
        <g:layoutHead/>
        <style>

        /*Hack for removing top nav link on quartz monitoring pages*/
        #mainContent > div.content > div.nav {
            display: none;
        }

        #mainContent > div.nav {
            display: none;
        }
        </style>
    </head>

    <body>
    <g:layoutBody/>
    </body>
    </html>
</g:applyLayout>
