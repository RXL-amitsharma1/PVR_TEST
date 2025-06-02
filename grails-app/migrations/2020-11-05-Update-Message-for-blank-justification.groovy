databaseChangeLog = {

    changeSet(author: "pranjal", id: "20201105025930-1") {
        preConditions(onFail: 'CONTINUE') {
            and{
                sqlCheck(expectedResult:'1', 'select count(*) from localization where code = \'app.label.justification.cannotbeblank\' and loc=\'*\'')
                not {
                    sqlCheck(expectedResult:'Justification can\'t be blank.', 'select text from localization where code = \'app.label.justification.cannotbeblank\' and loc=\'*\'')
                }
            }

        }
        sql("update localization set text='Justification can''t be blank.' where code='app.label.justification.cannotbeblank' and loc='*' ")
    }
}
