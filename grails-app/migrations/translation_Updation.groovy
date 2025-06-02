databaseChangeLog = {
    changeSet(author: "Devendra", id: "201902250163-1") {
        sql("update localization set text='はい' where code='default.button.yes.label' and loc='ja' ")
        sql("update localization set text='いいえ' where code='default.button.no.label' and loc='ja' ")

    }

    changeSet(author: "jitin (generated)", id: "2960211799019-4") {
        update(tableName: "LOCALIZATION", where: "CODE = 'etlcase.Transformation.label' and LOC = 'ja'") {
            column(name: "TEXT", value: "症例送信ステージ")
        }
    }

    changeSet(author: "ankita", id: "2960211799019-1") {
        sql("update localization set text='作成日' where code='app.label.action.item.create.date' and loc='ja' ")
        sql("update localization set text='ドラフトを作成' where code='reportCriteria.generate.draft' and loc='ja' ")
        sql("update localization set text='レポート実行時に作成されるアクションアイテム' where code='app.label.deliveryOptions.task.header' and loc='ja' ")
    }
    changeSet(author: "ankita", id: "2960211799020-1") {
        sql("update localization set text='テンプレートセット編集者' where code='app.role.ROLE_TEMPLATE_SET_CRUD' and loc='ja' ")
        sql("update localization set text='テンプレートセット閲覧者' where code='app.role.ROLE_TEMPLATE_SET_VIEW' and loc='ja' ")
        sql("update localization set text='レポートリクエストを割り当てる' where code='app.role.ROLE_REPORT_REQUEST_ASSIGN' and loc='ja' ")
        sql("update localization set text='PVQ閲覧者' where code='app.role.ROLE_PVQ_VIEW' and loc='ja' ")
        sql("update localization set text='PVQ編集者' where code='app.role.ROLE_PVQ_EDIT' and loc='ja' ")
        sql("update localization set text='カスタム式編集者' where code='app.role.ROLE_CUSTOM_EXPRESSION' and loc='ja' ")
        sql("update localization set text='カスタムフィールドエディタ' where code='app.role.ROLE_CUSTOM_FIELD' and loc='ja' ")
        sql("update localization set text='構成テンプレート作成者' where code='app.role.ROLE_CONFIG_TMPLT_CREATOR' and loc='ja' ")
        sql("update localization set text='ICSRレポート編集者' where code='app.role.ROLE_ICSR_REPORTS_EDITOR' and loc='ja' ")
        sql("update localization set text='ユーザーマネージャー' where code='app.role.ROLE_USER_MANAGER' and loc='ja' ")
        sql("update localization set text='APIトークン' where code='app.label.api.token' and loc='ja' ")
        sql("update localization set text='新しいトークンの生成' where code='app.label.generate.api.token' and loc='ja' ")
    }
    changeSet(author: "ankita", id: "2960211799021-1") {
        sql("update localization set text='消去できません {0}。' where code='default.unable.deleted.message' and loc='ja' ")
        sql("update localization set text='マイチーム' where code='app.label.mayTeam' and loc='ja' ")
        sql("update localization set text='PV Reports - レポートリクエストタイプ作成' where code='app.reportRequestType.create' and loc='ja' ")
        sql("update localization set text='PV Reports - レポートリクエストタイプ編集' where code='app.reportRequestType.edit' and loc='ja' ")
        sql("update localization set text='PV Reports - シングルサインオンエラー' where code='app.error.sso.title' and loc='ja' ")
        sql("update localization set text='ユーザートークンは一意でなければなりません' where code='com.rxlogix.user.User.apiToken.validator.invalid' and loc='ja' ")
        sql("update localization set text='ダッシュボードに移る' where code='error.500.message.go.to.dashboard' and loc='ja' ")
        sql("update localization set text='シングルサインオンログイン失敗' where code='error.sso.title' and loc='ja' ")
        sql("update localization set text='SMQ広域' where code='app.eventDictionary.smqb' and loc='ja' ")
        sql("update localization set text='SMQ狭域' where code='app.eventDictionary.smqn' and loc='ja' ")
        sql("update localization set text='リクエストID' where code='app.label.action.item.report.request.id' and loc='ja' ")
        sql("update localization set text='この移行を実行する権限がありません' where code='app.label.workflow.rule.forbidden' and loc='ja' ")
        sql("update localization set text='でトランジションを実行' where code='app.label.workflow.rule.autoExecuteInDays_strt' and loc='ja' ")
        sql("update localization set text='日自動的に' where code='app.label.workflow.rule.autoExecuteInDays_end' and loc='ja' ")
        sql("update localization set text='自動的に実行' where code='app.label.workflow.rule.executedAutomatically' and loc='ja' ")
        sql("update localization set text='ユーザー名とパスワードを入力してください。' where code='app.label.workflow.rule.fillLogon' and loc='ja' ")
        sql("update localization set text='間違ったログイン/パスワード！' where code='app.label.workflow.rule.approvl.fail' and loc='ja' ")
        sql("update localization set text='レポートリクエストが削除されました。' where code='app.notification.reportRequest.deleted' and loc='ja' ")
        sql("update localization set text='提出不可' where code='app.label.draftOnly' and loc='ja' ")
        sql("update localization set text='提出可' where code='app.label.submittable' and loc='ja' ")
        sql("update localization set text='提出不可' where code='app.label.notSubmittableWatermark' and loc='ja' ")
        sql("update localization set text='優先順位が必要です' where code='com.rxlogix.config.ReportRequest.priority.nullable' and loc='ja' ")
        sql("update localization set text='レポートリクエスト優先順位' where code='app.label.reportRequest.settings' and loc='ja' ")
        sql("update localization set text='レポートリクエスト設定' where code='app.label.reportRequestPriority.appName' and loc='ja' ")
        sql("update localization set text='レポートリクエストリンクタイプ' where code='app.label.reportRequestLinkType.appName' and loc='ja' ")
        sql("update localization set text='リンクタイプ' where code='app.label.reportRequestLinkType.link.type' and loc='ja' ")
        sql("update localization set text='リンクされたレポートリクエスト' where code='app.label.reportRequest.linked' and loc='ja' ")
        sql("update localization set text='レポートリクエストのリンク' where code='app.label.reportRequest.linking' and loc='ja' ")
        sql("update localization set text='辞書の要素' where code='app.label.dictionary.element' and loc='ja' ")
        sql("update localization set text='辞書の要素' where code='app.label.ReportRequest.link.dialog' and loc='ja' ")
        sql("update localization set text='デフォルトのレポートリクエスト割り当て先グループ' where code='userGroup.field.defaultAssignTo.label' and loc='ja' ")
        sql("update localization set text='デフォルトのレポートリクエスト割り当て先グループはすでに設定されています。これを設定するには、currentからチェックボックスを外します。' where code='com.rxlogix.config.UserGroup.defaultRRAssignTo.unique' and loc='ja' ")
        sql("update localization set text='レポートリクエストを割り当てる権限がなく、デフォルトの「割り当て先」グループが設定されていないため、レポートリクエストを作成できません。システム管理者に依頼して権限を設定してもらうか、デフォルトの「割り当て先」グループを定義してください。' where code='com.rxlogix.config.ReportRequest.noDefaultRRAssignTo' and loc='ja' ")
        sql("update localization set text='年' where code='scheduler.years' and loc='ja' ")
        sql("update localization set text='レポートリクエストは編集できません。 ステータスは\\ \"{0} \\\"です。' where code='app.reportRequest.edit.error.message' and loc='ja' ")
        sql("update localization set text='フィールド{0}の値が無効です' where code='app.label.field.invalid.value' and loc='ja' ")
        sql("update localization set text='値が重複しています：' where code='app.label.duplicate.values' and loc='ja' ")
        sql("update localization set text='今日が期日' where code='app.widget.dueToday' and loc='ja' ")
        sql("update localization set text='明日が期日' where code='app.widget.dueTomorrow' and loc='ja' ")
        sql("update localization set text='3-5日以内に期日到来' where code='app.widget.due5' and loc='ja' ")
        sql("update localization set text='私（私は所有者です）' where code='app.widget.reportRequest.owner' and loc='ja' ")
        sql("update localization set text='私からリクエストされた' where code='app.widget.reportRequest.requested' and loc='ja' ")
        sql("update localization set text='私のグループからリクエストされた' where code='app.widget.reportRequest.requestedGroup' and loc='ja' ")
        sql("update localization set text='私に割り当てられた' where code='app.widget.reportRequest.assigned' and loc='ja' ")
        sql("update localization set text='私のグループに割り当てられた' where code='app.widget.reportRequest.assignedGroups' and loc='ja' ")
        sql("update localization set text='私のチームに' where code='app.widget.reportRequest.assigned.team' and loc='ja' ")
        sql("update localization set text='全て' where code='app.widget.reportRequest.all' and loc='ja' ")
        sql("update localization set text='無題' where code='app.widget.reportRequest.no.title' and loc='ja' ")
        sql("update localization set text='レポートリクエスト' where code='app.widget.button.advancedReportRequest.label' and loc='ja' ")
        sql("update localization set text='ケースを更新して最終版を生成' where code='app.reportActionType.GENERATE_CASES_FINAL' and loc='ja' ")
        sql("update localization set text='ケースを更新してドラフトを生成' where code='app.reportActionType.GENERATE_CASES_DRAFT' and loc='ja' ")
        sql("update localization set text='ケースシリーズを更新' where code='app.reportActionType.GENERATE_CASES' and loc='ja' ")
        sql("update localization set text='アーカイブ' where code='app.reportActionType.ARCHIVE' and loc='ja' ")
        sql("update localization set text='エラー' where code='app.label.icsr.error' and loc='ja' ")
        sql("update localization set text='こんにちは！<br> <br>レポート<a href=\\\"{0}\\\"> {1} </a>が文書管理システムに正常にアップロードされました。 ファイル名：{2}。' where code='app.dms.file.transmitted.message' and loc='ja' ")
        sql("update localization set text='文書管理システムにアップロードされたレポート' where code='app.dms.file.transmitted.title' and loc='ja' ")
        sql("update localization set text='こんにちは、\\n {0}用に生成したPV Analytics解析ファイルは完成しました。 リクエストされたPV Analytics解析ファイルの詳細は以下のとおりです。' where code='spotfire.email.message1' and loc='ja' ")
        sql("update localization set text='製品ファミリー名：{0} \\nレポート期間日付範囲：{1}から{2} \\n{3}日現在 \\nファイル名：{4} \\nファイル生成リクエストの日付/時刻：{5}' where code='spotfire.email.message2' and loc='ja' ")
        sql("update localization set text='解析ファイルを開くには：\\n1. {0}に移動します。\\n2. ユーザー名とパスワードを使用してアプリケーションにログインします。\\n3. ページの検索ボックスに、上記のファイル名を入力してください。\\n4. 上記のファイル名に対応するアクション欄の[表示]ボタンをクリックしてください。\\n5. PV Analytics解析ファイルが新しいタブで開きます' where code='spotfire.email.message3' and loc='ja' ")
        sql("update localization set text='あなたのアカウントは期限が切れました。' where code='springSecurity.errors.login.expired' and loc='ja' ")
        sql("update localization set text='あなたのパスワードは期限が切れました。' where code='springSecurity.errors.login.passwordExpired' and loc='ja' ")
        sql("update localization set text='あなたのアカウントは無効になっています。' where code='springSecurity.errors.login.disabled' and loc='ja' ")
        sql("update localization set text='あなたのアカウントはロックされています。' where code='springSecurity.errors.login.locked' and loc='ja' ")
        sql("update localization set text='そのユーザー名とパスワードを持つユーザーを見つけることができませんでした。' where code='springSecurity.errors.login.fail' and loc='ja' ")
        sql("update localization set text='SSOログインユーザーアカウントがアプリケーションで無効になっています。' where code='springSecurity.errors.login.sso.disabled' and loc='ja' ")
        sql("update localization set text='SSOログインユーザーアカウントがアプリケーションでロックされています。' where code='springSecurity.errors.login.sso.locked' and loc='ja' ")
        sql("update localization set text='ユーザーはアプリケーションにアクセスする権限を持っていません。 システム管理者に連絡してください。' where code='springSecurity.errors.login.sso.notfound' and loc='ja' ")
        sql("update localization set text='新しいセクション' where code='app.executionStatus.aggregateReportStatus.GENERATING_NEW_SECTION' and loc='ja' ")
        sql("update localization set text='ケースシリーズ' where code='app.executionStatus.aggregateReportStatus.GENERATED_CASES' and loc='ja' ")
        sql("update localization set text='ドラフト' where code='app.executionStatus.aggregateReportStatus.GENERATING_DRAFT' and loc='ja' ")
        sql("update localization set text='最終版' where code='app.executionStatus.aggregateReportStatus.GENERATING_FINAL_DRAFT' and loc='ja' ")
        sql("update localization set text='ドラフト' where code='app.executionStatus.aggregateReportStatus.GENERATED_DRAFT' and loc='ja' ")
        sql("update localization set text='最終版' where code='app.executionStatus.aggregateReportStatus.GENERATED_FINAL_DRAFT' and loc='ja' ")
        sql("update localization set text='新しいセクション' where code='app.executionStatus.aggregateReportStatus.GENERATED_NEW_SECTION' and loc='ja' ")
        sql("update localization set text='完了しました' where code='app.executionStatus.aggregateReportStatus.COMPLETED' and loc='ja' ")
        sql("update localization set text='エラー' where code='app.executionStatus.aggregateReportStatus.ERROR' and loc='ja' ")
        sql("update localization set text='ケースを更新して最終版を生成' where code='app.reportActionType.GENERATE_CASES_FINAL' and loc='ja' ")
        sql("update localization set text='警告' where code='app.executionStatus.aggregateReportStatus.WARN' and loc='ja' ")
        sql("update localization set text='ファイルを確認して再添付してください' where code='app.reportRequest.attachment.warning' and loc='ja' ")
        sql("update localization set text='添付ファイルがありません' where code='app.reportRequest.attachment.empty.message' and loc='ja' ")
        sql("update localization set text='ユーザー' where code='com.rxlogix.enums.EmailTemplateTypeEnum.USER' and loc='ja' ")
        sql("update localization set text='メールテンプレート' where code='app.label.emailTemplate.appName' and loc='ja' ")
        sql("update localization set text='=リンクを確認して必要に応じて再定義してください' where code='app.reportRequest.link.warning' and loc='ja' ")
        sql("update localization set text='リンクされたレポートリクエストはありません' where code='app.reportRequest.link.empty.message' and loc='ja' ")
        sql("update localization set text='症例番号' where code='app.caseNumber.label' and loc='ja' ")
        sql("update localization set text='アクションアイテム説明は必須です' where code='app.action.item.description.nullable' and loc='ja' ")
        sql("update localization set text='Spotfireファイルを生成する' where code='app.spotfire.caseSeries.generate.spotfire' and loc='ja' ")
        sql("update localization set text='データ解析を有効にするには、\\\"製品の選択\\\"フィールドで少なくとも1つの\\\"製品ファミリー\\\"を選択する必要があります。' where code='app.spotfire.enable.note' and loc='ja' ")
        sql("update localization set text='セクションリストが変更されました、添付ファイルを確認してください' where code='app.label.additionalAttachmentsWarning' and loc='ja' ")
        sql("update localization set text='添付ファイルを追加する' where code='app.label.additionalAttachments.addAttachment' and loc='ja' ")
        sql("update localization set text='少なくとも1つのセクションと少なくとも1つのフォーマットを選択してください' where code='app.label.additionalAttachmentsDialogError' and loc='ja' ")
        sql("update localization set text='有効なメールアドレスを入力してください' where code='app.error.choose.email.required' and loc='ja' ")
        sql("update localization set text='ログアウトされました。' where code='logout.local.title' and loc='ja' ")
        sql("update localization set text='スタートページへ' where code='logout.local.link.start' and loc='ja' ")

    }

    changeSet(author: "devendra", id: "201903110336-1") {
        sql("update localization set text='添付ファイル' where code='app.label.attachments' and loc='ja' ")
        sql("update localization set text='添付' where code='app.label.attach' and loc='ja' ")
    }

    changeSet(author: "devendra", id: "201903110417-1"){
        sql("update localization set text='日後' where code='app.label.deliveryOptions.task.days.after' and loc='ja' ")
        sql("update localization set text='から' where code='app.label.deliveryOptions.task.from' and loc='ja' ")
        sql("update localization set text='前' where code='app.label.deliveryOptions.task.before' and loc='ja' ")
        sql("update localization set text='後' where code='app.label.deliveryOptions.task.after' and loc='ja' ")
        sql("update localization set text='タスクテンプレートリスト' where code='app.label.deliveryOptions.task.template.list' and loc='ja' ")
        sql("update localization set text='報告リクエストテンプレート' where code='app.TaskTemplateTypeEnum.REPORT_REQUEST' and loc='ja' ")
        sql("update localization set text='集積報告テンプレート' where code='app.TaskTemplateTypeEnum.AGGREGATE_REPORTS' and loc='ja' ")
        sql("update localization set text='タスクの説明は空には出来ません' where code='com.rxlogix.config.TaskTemplate.reportTasks.description.empty' and loc='ja' ")
        sql("update localization set text='タスクの名前は空には出来ません' where code='com.rxlogix.config.TaskTemplate.tasks.description.empty' and loc='ja' ")
        sql("update localization set text='最低一つのタスクが定義されていなければなりません' where code='com.rxlogix.config.TaskTemplate.alltasks.empty' and loc='ja' ")
        sql("update localization set text='オーナーにアサイン' where code='com.rxlogix.config.TaskTemplate.assignToOwner' and loc='ja' ")
        sql("update localization set text='実行日時' where code='com.rxlogix.config.TaskTemplate.execution.date' and loc='ja' ")
        sql("update localization set text='PV レポート - 一括更新' where code='app.PeriodicReport.bulkScheduling.title' and loc='ja' ")
        sql("update localization set text='公開' where code='com.rxlogix.enums.EmailTemplateTypeEnum.PUBLIC' and loc='ja' ")
        sql("update localization set text='Eメールテンプレートを削除できません' where code='app.label.emailTemplate.delete.error.message' and loc='ja' ")
        sql("update localization set text='PVレポート - Eメールテンプレート作成' where code='app.emailTemplateConfiguration.create.title' and loc='ja' ")
        sql("update localization set text='Eメールテンプレート作成' where code='app.label.createemailTemplate' and loc='ja' ")
        sql("update localization set text='Eメールテンプレート編集' where code='app.label.editemailTemplate' and loc='ja' ")
        sql("update localization set text='PVレポート - Eメールテンプレート編集' where code='app.emailTemplateConfiguration.edit.title' and loc='ja' ")
        sql("update localization set text='PVレポート - Eメールテンプレートリスト' where code='app.emailTemplateConfiguration.list.title' and loc='ja' ")
        sql("update localization set text='PVレポート - Eメールテンプレート閲覧' where code='app.emailTemplateConfiguration.show.title' and loc='ja' ")
        sql("update localization set text='Eメールテンプレート閲覧' where code='app.label.viewemailTemplate' and loc='ja' ")
        sql("update localization set text='テンプレートリスト' where code='app.label.emailConfiguration.temlateList' and loc='ja' ")
    }

    changeSet(author: "ankita", id: "201902041302-1") {
        sql("update localization set text='値を貼り付け/インポートする' where code='paste.or.import.values' and loc='ja' ")
    }

    changeSet(author: "ankita", id: "201903110338-1") {
        sql("update localization set text='状態が更新されました。ケースシリーズの更新とドラフトレポートの生成がスケジュールされており、完了時に配信されます。最新の実行ステータスを確認するには更新してください' where code='app.periodicReportConfiguration.state.update.GENERATE_CASES_FINAL' and loc='ja' ")
        sql("update localization set text='状態が更新されました。ケースシリーズの更新がスケジュールされており、完了時に配信されます。最新の実行ステータスを確認するには更新してください' where code='app.periodicReportConfiguration.state.update.GENERATE_CASES' and loc='ja' ")
        sql("update localization set text='状態が更新されました。ケースシリーズの更新とドラフトレポートの生成がスケジュールされており、完了時に配信されます。最新の実行ステータスを確認するには更新してください' where code='app.periodicReportConfiguration.state.update.GENERATE_CASES_DRAFT' and loc='ja' ")
    }
  
    changeSet(author: "Devendra", id: "201903281223-1") {
        sql("update localization set text='表示するデータがありません' where code='app.label.widget.noData' and loc='ja' ")
        sql("update localization set text='全レポート' where code='app.label.entire.report' and loc='ja' ")
    }
    changeSet(author: "Devendra", id: "201904191223-1") {
        sql("update localization set text='カレンダー' where code='default.button.addCalendarWidget.label' and loc='ja' ")
    }

    changeSet(author: "Shubham", id: "201904261226-1"){
        sql("update localization set text = '行数合計' where code = 'app.label.subTotalRowsNumber' and loc='ja' ")
        sql("update localization set text = '行数合計' where code = 'app.label.totalRowsNumber' and loc='ja' ")
        sql("update localization set text = '症例数合計' where code = 'app.label.subTotalCaseNumber' and loc='ja' ")
        sql("update localization set text = '症例数合計' where code = 'app.label.totalCaseNumber' and loc='ja' ")
        sql("update localization set text = 'フィルターされた行数' where code = 'app.label.filteredRowsNumber' and loc='ja' ")
        sql("update localization set text = 'フィルターされた症例数' where code = 'app.label.filteredCaseNumber' and loc='ja' ")
    }

    changeSet(author: "Shubham", id: "201904260113"){
        sql("update localization set text = 'Stateを更新しました。ファイナルレポートがスケジュールされ、完了時に配布されます。リフレッシュして最新の実行状態をチェックしてください。' where code ='app.periodicReportConfiguration.state.update.GENERATE_CASES_FINAL' and loc='ja' ")
        sql("update localization set text = 'Stateを更新しました。ケースシリーズの更新とドラフトレポートの生成がスケジュールされており、完了時に配信されます。リフレッシュして最新の実行状態をチェックしてください。' where code ='app.periodicReportConfiguration.state.update.GENERATE_CASES_DRAFT' and loc='ja' ")
    }

    changeSet(author: "Devendra", id: "201905030113"){
        sql("update localization set text='いいえ 割合' where code='app.percentageOptionEnum.NO_PERCENTAGE' and loc='ja' ")
    }

    changeSet(author: "Ankita", id: "201905141756-2"){
        sql("update localization set text = '{0} {1} を削除しました' where code ='default.deleted.message' and loc='ja' ")
        sql("update localization set text = '{0} {1} を作成しました' where code ='default.created.message' and loc='ja' ")
        sql("update localization set text = '{0} {1} を更新しました' where code ='default.updated.message' and loc='ja' ")
    }

    changeSet(author: "Ankita", id: "201906121211-1"){
        sql("update localization set text = 'ステータスを更新しました。ドラフトレポートの作成がスケジュールされ、完了時に配信されます。リフレッシュボタンにより最新の実行状態を確認してください。' where code ='app.periodicReportConfiguration.state.update.GENERATE_DRAFT' and loc='ja' ")
    }

    changeSet(author: "Anshul", id: "201906201906-6"){
        sql("update localization set text = '抽出された症例の完全な症例情報を出力します。例えば、「発現までの時間」は選択された製品であるかに関わらず、症例内の全ての製品に対して出力されます。' where code ='app.query.level.case.help' and loc='ja' ")
        sql("update localization set text = '選択された製品に限定して情報を出力します。例えば、「発現までの時間」は選択された製品に対してのみ出力されます。' where code ='app.query.level.product.help' and loc='ja' ")
        sql("update localization set text = '選択された製品および有害事象に関連する症例情報のみを出力します。例えば、「発現までの時間」は選択された製品および有害事象に対してのみ出力されます。' where code ='app.query.level.product.and.event.help' and loc='ja' ")
        sql("update localization set text = '指定した条件に一致する症例の送信情報が出力されます。' where code ='app.query.level.submission.help' and loc='ja' ")
        sql("update localization set text = '選択された有害事象に限定して情報を出力します。例えば、「発現までの時間」は選択された有害事象に対してのみ出力されます。' where code ='app.query.level.event.help' and loc='ja' ")
        sql("update localization set text = 'テンプレート/クエリ作成ページに転送されます。 保存ボタンを押すとこのページに戻ります' where code ='app.template.query.create.warning' and loc='ja' ")
    }

    changeSet(author: "Anshul", id: "201927201927-3"){
        sql("update localization set text = 'Please find attached report.' where code ='app.label.emailConfiguration.email.delivery' and loc='*' ")
        sql("update localization set text = '添付の報告書を見つけてください。' where code ='app.label.emailConfiguration.email.delivery' and loc='ja' ")
    }

    changeSet(author: "Devendra", id: "201907020335") {
        sql("update localization set text='Mine (I''m the owner)' where code='app.label.iAmOwner' and loc='*' ")
        sql("update localization set text='Mine (I''m Owner)' where code='app.widget.reportRequest.owner' and loc='*' ")
        sql("update localization set text='自己（私が所有者)' where code='app.label.iAmOwner' and loc='ja' ")
        sql("update localization set text='自己（私は所有者です)' where code='app.widget.reportRequest.owner' and loc='ja' ")
    }

    changeSet(author: "Sargam", id: "201907050406"){
        sql("update localization set text='To open your analysis file: \n1. Navigate to {0} \n2. Log into application using username and password \n3. In the Search box on the page, Enter the File Name above \n4. Click on View button in action column corresponding to the above filename \n5. The PV Analytics analysis file will open in the new tab' where code='spotfire.email.message3' and loc='*' ")
    }

    changeSet(author: "Rishabh", id: "20190823115045") {
        sql("update localization set text='テンプレートセットのあるレポートはダウンロード専用としてのみサポートされています。表示するにはレポートをダウンロードしてください。' where code='app.report.templateSet.htmlNotSupported' and loc='ja' ")
    }

    changeSet(author: "Vinay", id: "1577188322522") {
        sql("update localization set text='ICSR PROFILE CONFIGURATION' where code='app.icsrProfileConf.label.myInbox' and loc='*' ")
    }

    changeSet(author: "Vinay", id: "1577188528922") {
        sql("update localization set text='ICSR Profile Name' where code='app.icsrProfileConf.label.icsrPartnerName' and loc='*' ")
    }

    changeSet(author: "Vinay", id: "1577188575058") {
        sql("update localization set text='ICSR PROFILE CONFIGURATION' where code='app.label.icsr.profile.conf.reportRecipientConfiguration' and loc='*' ")
    }

    changeSet(author: "Vinay", id: "1577188646965") {
        sql("update localization set text='ICSR Case Tracking' where code='iscr.case.tracking.label' and loc='*' ")
    }

    changeSet(author: "Sachin", id: "20190823115075") {
        sql("update localization set text='{0} with value {2} does not match the required pattern {3}' where code='default.doesnt.match.message' and loc='*'")
        sql("update localization set text='{0} with value {2} is not a valid URL' where code='default.invalid.url.message' and loc='*'")
        sql("update localization set text='{0} with value {2} is not a valid credit card number' where code='default.invalid.creditCard.message' and loc='*'")
        sql("update localization set text='{0} with value {2} is not a valid e-mail address' where code='default.invalid.email.message' and loc='*'")
        sql("update localization set text='{0} with value {2} does not fall within the valid range from {3} to {4}' where code='default.invalid.range.message' and loc='*'")
        sql("update localization set text='{0} with value {2} does not fall within the valid size range from {3} to {4}' where code='default.invalid.size.message' and loc='*'")
        sql("update localization set text='{0} with value {2} exceeds maximum value {3}' where code='default.invalid.max.message' and loc='*'")
        sql("update localization set text='{0} with value {2} is less than minimum value {3}' where code='default.invalid.min.message' and loc='*'")
        sql("update localization set text='{0} with value {2} exceeds the maximum size of {3}' where code='default.invalid.max.size.message' and loc='*'")
        sql("update localization set text='{0} with value {2} is less than the minimum size of {3}' where code='default.invalid.min.size.message' and loc='*'")
        sql("update localization set text='{0} with value {2} does not pass custom validation' where code='default.invalid.validator.message' and loc='*'")
        sql("update localization set text='{0} with value {2} is not contained within the list {3}' where code='default.not.inlist.message' and loc='*'")
        sql("update localization set text='{0} cannot be blank' where code='default.blank.message' and loc='*'")
        sql("update localization set text='{0} with value {2} cannot equal {3}' where code='default.not.equal.message' and loc='*'")
        sql("update localization set text='{0} cannot be null' where code='default.null.message' and loc='*'")
        sql("update localization set text='{0} with value {2} must be unique' where code='default.not.unique.message' and loc='*'")
    }

    changeSet(author: "Sachin", id: "20190823115076") {
        sql("update localization set text='{0}の値{2}は、要求されたパターン{3}と一致しません' where code='default.doesnt.match.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、有効なURLではありません' where code='default.invalid.url.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、有効なクレジットカード番号ではありません' where code='default.invalid.creditCard.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、有効なEメールアドレスではありません' where code='default.invalid.email.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、有効な範囲{3}から{4}にありません' where code='default.invalid.range.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、有効なサイズの範囲{3}から{4}にありません' where code='default.invalid.size.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、最大値{3}を超過しています' where code='default.invalid.max.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、最小値{3}未満です' where code='default.invalid.min.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、最大サイズ{3}を超過しています' where code='default.invalid.max.size.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、最小サイズ{3}未満です' where code='default.invalid.min.size.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、カスタムバリデーションをパスしません' where code='default.invalid.validator.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、リスト{3}に含まれません' where code='default.not.inlist.message' and loc='ja'")
        sql("update localization set text='{0}を、空白にすることはできません' where code='default.blank.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、{3}と同一にはできません' where code='default.not.equal.message' and loc='ja'")
        sql("update localization set text='{0}を、nullにすることはできません' where code='default.null.message' and loc='ja'")
        sql("update localization set text='{0}の値{2}は、ユニーク値にする必要があります' where code='default.not.unique.message' and loc='ja'")
    }

    changeSet(author: "Sachin", id: "20190823115046") {
        sql("update localization set text='Never Logged In' where code='user.neverLoggedIn.before.label' ")
    }

    changeSet(author: "Sachin", id: "20190823115087") {
        sql("update localization set text='{0} exceeds maximum value {3}' where code='default.invalid.max.message' and loc='*'")
        sql("update localization set text='{0} exceeds the maximum size of {3}' where code='default.invalid.max.size.message' and loc='*'")
        sql("update localization set text='{0} does not pass validation' where code='default.invalid.validator.message' and loc='*'")
        sql("update localization set text='{0} does not pass validation' where code='default.invalid.validator.message' and loc='*'")
        sql("update localization set text='{0} does not pass validation' where code='default.invalid.validator.message' and loc='*'")
        sql("update localization set text='Product Selection cannot be greater than {3} characters' where code='com.rxlogix.config.Configuration.productSelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Study Selection cannot be greater than {3} characters' where code='com.rxlogix.config.Configuration.studySelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Event Selection cannot be greater than {3} characters' where code='com.rxlogix.config.Configuration.eventSelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Schedule Date JSON cannot be greater than {3} characters' where code='com.rxlogix.config.Configuration.scheduleDateJSON.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Completed JSON Query cannot be greater than {3} characters' where code='com.rxlogix.config.Configuration.completedJSONQuery.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Product Selection cannot be greater than {3} characters' where code='com.rxlogix.config.executed.caseSeries.productSelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Study Selection cannot be greater than {3} characters' where code='com.rxlogix.config.executed.caseSeries.studySelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Schedule Date JSON cannot be greater than {3} characters' where code='com.rxlogix.config.ExecutedConfiguration.scheduleDateJSON.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Completed JSON Query cannot be greater than {3} characters' where code='com.rxlogix.config.ExecutedConfiguration.completedJSONQuery.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Completed JSON Query cannot be greater than {3} characters' where code='com.rxlogix.config.SuperQuery.JSONQuery.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Category cannot be greater than {3} characters' where code='com.rxlogix.config.CaseLineListingTemplate.category.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Category cannot be greater than {3} characters' where code='com.rxlogix.config.DataTabulationTemplate.category.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Category cannot be greater than {3} characters' where code='com.rxlogix.config.ExecutedCaseLineListingTemplate.category.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Category cannot be greater than {3} characters' where code='com.rxlogix.config.ExecutedDataTabulationTemplate.category.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Product Selection cannot be greater than {3} characters' where code='com.rxlogix.config.PeriodicReportConfiguration.productSelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Study Selection cannot be greater than {3} characters' where code='com.rxlogix.config.PeriodicReportConfiguration.studySelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Schedule Date JSON cannot be greater than {3} characters' where code='com.rxlogix.config.PeriodicReportConfiguration.scheduleDateJSON.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Completed JSON Query cannot be greater than {3} characters' where code='com.rxlogix.config.PeriodicReportConfiguration.completedJSONQuery.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Product Selection cannot be greater than {3} characters' where code='com.rxlogix.config.ReportRequest.productSelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Study Selection cannot be greater than {3} characters' where code='com.rxlogix.config.ReportRequest.studySelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Event Selection cannot be greater than {3} characters' where code='com.rxlogix.config.ReportRequest.eventSelection.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Name cannot be greater than {3} characters' where code='com.rxlogix.config.CustomReportField.customName.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Description cannot be greater than {3} characters' where code='com.rxlogix.config.CustomReportField.description.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Expression cannot be greater than {3} characters' where code='com.rxlogix.config.CustomReportField.defaultExpression.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Name cannot be greater than {3} characters' where code='com.rxlogix.commandObjects.CustomReportFieldCO.customName.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Description cannot be greater than {3} characters' where code='com.rxlogix.commandObjects.CustomReportFieldCO.description.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Expression cannot be greater than {3} characters' where code='com.rxlogix.commandObjects.CustomReportFieldCO.defaultExpression.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Name cannot be greater than {3} characters' where code='com.rxlogix.config.EmailTemplate.name.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Email body cannot be greater than {3} characters' where code='com.rxlogix.config.EmailTemplate.body.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Email cannot be greater than {3} characters' where code='com.rxlogix.config.Email.email.maxSize.exceeded' and loc='*'")
        sql("update localization set text='Description cannot be greater than {3} characters' where code='com.rxlogix.config.Email.description.maxSize.exceeded' and loc='*'")
    }


    changeSet(author: "Sachin", id: "20190823115090"){
    sql("update localization set text='{0}は、最大値{3}を超過しています' where code='default.invalid.max.message' and loc='ja'")
    sql("update localization set text='{0}は、最大サイズ{3}を超過しています' where code='default.invalid.max.size.message' and loc='ja'")
    sql("update localization set text='{0}は、カスタムバリデーションをパスしません' where code='default.invalid.validator.message' and loc='ja'")
    sql("update localization set text='商品の選択は{3}文字を超えることはできません' where code='com.rxlogix.config.Configuration.productSelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='学習選択は{3}文字を超えることはできません' where code='com.rxlogix.config.Configuration.studySelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='イベント選択は{3}文字を超えることはできません' where code='com.rxlogix.config.Configuration.eventSelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='スケジュール日付JSONは{3}文字を超えることはできません' where code='com.rxlogix.config.Configuration.scheduleDateJSON.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='完成したJSONクエリは{3}文字を超えることはできません' where code='com.rxlogix.config.Configuration.completedJSONQuery.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='商品の選択は{3}文字を超えることはできません' where code='com.rxlogix.config.executed.caseSeries.productSelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='スケジュール日付JSONは{3}文字を超えることはできません' where code='com.rxlogix.config.ExecutedConfiguration.scheduleDateJSON.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='完成したJSONクエリは{3}文字を超えることはできません' where code='com.rxlogix.config.ExecutedConfiguration.completedJSONQuery.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='完成したJSONクエリは{3}文字を超えることはできません' where code='com.rxlogix.config.SuperQuery.JSONQuery.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='カテゴリは{3}文字以下にしてください' where code='com.rxlogix.config.DataTabulationTemplate.category.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='カテゴリは{3}文字以下にしてください' where code='com.rxlogix.config.ExecutedCaseLineListingTemplate.category.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='カテゴリは{3}文字以下にしてください' where code='com.rxlogix.config.ExecutedDataTabulationTemplate.category.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='商品の選択は{3}文字を超えることはできません' where code='com.rxlogix.config.PeriodicReportConfiguration.productSelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='学習選択は{3}文字を超えることはできません' where code='com.rxlogix.config.PeriodicReportConfiguration.studySelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='スケジュール日付JSONは{3}文字を超えることはできません' where code='com.rxlogix.config.PeriodicReportConfiguration.scheduleDateJSON.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='完成したJSONクエリは{3}文字を超えることはできません' where code='com.rxlogix.config.PeriodicReportConfiguration.completedJSONQuery.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='商品の選択は{3}文字を超えることはできません' where code='com.rxlogix.config.ReportRequest.productSelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='学習選択は{3}文字を超えることはできません' where code='com.rxlogix.config.ReportRequest.studySelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='イベント選択は{3}文字を超えることはできません' where code='com.rxlogix.config.ReportRequest.eventSelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='名前は{3}文字以下にする必要があります。' where code='com.rxlogix.config.CustomReportField.customName.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='説明は{3}文字を超えることはできません。' where code='com.rxlogix.config.CustomReportField.description.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='式は{3}文字を超えることはできません。' where code='com.rxlogix.config.CustomReportField.defaultExpression.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='名前は{3}文字以下にする必要があります。' where code='com.rxlogix.commandObjects.CustomReportFieldCO.customName.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='説明は{3}文字を超えることはできません。' where code='com.rxlogix.commandObjects.CustomReportFieldCO.description.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='式は{3}文字を超えることはできません。' where code='com.rxlogix.commandObjects.CustomReportFieldCO.defaultExpression.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='Name cannot be greater than {3} characters' where code='com.rxlogix.commandObjects.ReportFieldCO.name.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='Name cannot be greater than {3} characters' where code='com.rxlogix.config.EmailTemplate.name.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='Email body cannot be greater than {3} characters' where code='com.rxlogix.config.EmailTemplate.body.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='Email cannot be greater than {3} characters' where code='com.rxlogix.config.Email.email.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='Description cannot be greater than {3} characters' where code='com.rxlogix.config.Email.description.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='学習選択は{3}文字を超えることはできません' where code='com.rxlogix.config.executed.caseSeries.studySelection.maxSize.exceeded' and loc='ja'")
    sql("update localization set text='カテゴリは{3}文字以下にしてください' where code='com.rxlogix.config.CaseLineListingTemplate.category.maxSize.exceeded' and loc='ja'")
        }

    changeSet(author: "Sachin", id: "20190823115093") {
        sql("update localization set text=replace(text,'can\'\'t',q'\$can\'\'t\$') where text like '%can\'\'t%'")
    }


    changeSet(author: "Shubham", id: "20200122042223") {
        sql("update localization set text='重要でないフォローアップを含める' where code='reportCriteria.include.non.significant.followup.cases' and loc='ja' ")
    }

    changeSet(author: "Shubham", id: "20201123015512") {
        sql("update localization set text='ICSR XML' where code='app.templateType.XML' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20201123015721") {
        sql("update localization set text='New ICSR XML Template' where code='app.newTemplate.xml.menu' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20201125125655") {
        sql("update localization set text='ICSR Organization Configuration' where code='app.label.unit.configuration.menuItem' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20201125125715") {
        sql("update localization set text='Organization Configuration' where code='app.unitConfiguration.label.myInbox' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20201125125950") {
        sql("update localization set text='E2B Routing ID' where code='app.unitConfiguration.label.unitRegisteredID' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20201125125955") {
        sql("update localization set text='E2B Routing ID is required' where code='com.rxlogix.config.UnitConfiguration.unitRegisteredId.nullable' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20201125125960") {
        sql("update localization set text='E2B Routing ID is already in use, please use a different name' where code='com.rxlogix.config.UnitConfiguration.unitRegisteredId.unique' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20201125014122") {
        sql("update localization set text='Disabled' where code='app.unitConfiguration.label.unitRetired' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20201125014227") {
        sql("update localization set text='Disabled must be chosen' where code='com.rxlogix.config.UnitConfiguration.unitRetired.nullable' and loc='*' ")
    }
  
    changeSet(author: "Shubham", id: "20201124030110-1") {
        sql("update localization set text='Include Comparators' where code='reportCriteria.include.all.study.drugs.cases' and loc='*' ")
        sql("update localization set text='ICSR Recipient' where code='app.label.icsr.profile.conf.icsrProfileConfiguration' and loc='*' ")
        sql("update localization set text='Incoming Ack Folder' where code='app.label.icsr.profile.conf.incomingFolder' and loc='*' ")
        sql("update localization set text='Disable' where code='app.label.icsr.profile.conf.active' and loc='*' ")
        sql("update localization set text='Gateway' where code='app.distributionChannel.E2B' and loc='*' ")
        sql("update localization set text='Manual' where code='app.distributionChannel.PAPER_MAIL' and loc='*' ")

    }

    changeSet(author: "Shubham", id: "20201203025410-1") {
        sql("update localization set text='ICSR Recipient' where code='app.label.icsr.profile.conf.icsrProfileConfiguration' and loc='ja' ")
        sql("update localization set text='ICSR Organization Configuration' where code='app.label.unit.configuration.menuItem' and loc='ja' ")
    }

    changeSet(author: "Shubham", id: "20210115065251-1") {
        sql("update localization set text='Mr.' where code='app.title.MR' and loc='*' ")
        sql("update localization set text='Mrs.' where code='app.title.MRS' and loc='*' ")
        sql("update localization set text='Miss.' where code='app.title.MISS' and loc='*' ")
        sql("update localization set text='Ms.' where code='app.title.MS' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20210222015333-1") {
        sql("update localization set text='Case Series & Data Analysis' where code='app.label.case.new.series' and loc='*' ")
    }

    changeSet(author: "sergey", id: "202102211154-1") {
        sql("update localization set text='You are not authorized to view that page.' where code='error.403.message' and loc='*' ")
    }

    changeSet(author: "Sachin Verma", id: "20210223062112-1") {
        sql("update localization set text='Include All Study Drugs Cases' where code='reportCriteria.include.all.study.drugs.cases' and loc='*' ")
    }

    changeSet(author: "Shubham", id: "20210224015333-1") {
        sql("update localization set text='New Case Series' where code='app.label.case.new.series' and loc='*' ")
    }

    changeSet(author: "Sachin Verma", id: "20210311022333-1") {
        sql("update localization set text='F/U #' where code='app.label.followUpType' and loc='*' ")
        sql("update localization set text='Parsing error occured in attached R3 acknowledgement file : {0}' where code='app.emailService.acknowledgement.error.message.label' and loc='*' ")
    }

    changeSet(author: "ShubhamRx", id: "2021032506232322-1") {
        sql("update localization set text='Product Family Name: {0} \\nReporting Period Date Range: {1} to {2} \\nAs Of Date: {3} \\nCase Series Name: {4} \\nFile Name: {5} \\nFile Generation Request Date/Time: {6}' where code='spotfire.email.message2' and loc='*' ")
        sql("update localization set text='製品ファミリー名：{0} \\nレポート期間日付範囲：{1}から{2} \\n{3}日現在 \\n ケースシリーズ名: {4} \\nファイル名：{5} \\nファイル生成リクエストの日付/時刻：{6}' where code='spotfire.email.message2' and loc='ja' ")
    }

    changeSet(author: "Sachin Verma", id: "20210404130800-1") {
        sql("update localization set text='F/U Type' where code='app.label.followUpInfo' and loc='*' ")
    }

    changeSet(author: "sergey", id: "202104121154-1") {
        sql("update localization set text='Warning! Report is outdated, please refresh.' where code='app.pvc.dirty' and loc='*' ")
        sql("update localization set text='Warning! Report is outdated, please refresh.' where code='app.pvc.dirty' and loc='ja' ")
    }
   changeSet(author: "sergey", id: "202105181154-1") {
        sql("update localization set text='Data was successfully imported' where code='app.pvc.import.importSuccess' and loc='*' ")
        sql("update localization set text='Data was successfully imported' where code='app.pvc.import.importSuccess' and loc='ja' ")
    }

    changeSet(author: "ShubhamRx", id: "20210504080829-1") {
        sql("update localization set text='Results in Death (Y/N);' where code='app.caseList.FLAG_SERIOUS_DEATH' and loc='*' ")
        sql("update localization set text='Product Name' where code='app.caseList.PROD_NAME_RESOLVED' and loc='*' ")
        sql("update localization set text='Reported Product Name' where code='app.caseList.PRIM_PROD_NAME' and loc='*' ")
        sql("update localization set text='Product Name/Study Drug (Coded)' where code='app.caseList.PRODUCT_NAME' and loc='*' ")
        sql("delete from localization where code='app.caseList.PROD_ID_RESOLVED' ")
    }

    changeSet(author: "ShubhamRx", id: "20210504080829-2") {
        sql("update localization set text='All Conmeds' where code='app.caseList.CONCOMIT_PROD_2_ALL' and loc='*' ")
        sql("update localization set text='All Suspect Products' where code='app.caseList.COMPANY_SUSP_PROD_ALL' and loc='*' ")
        sql("update localization set text='Batch/Lot#' where code='app.caseList.LOT_NO_ALL' and loc='*' ")
        sql("update localization set text='Case Classification' where code='app.caseList.CHARACTERISTIC_ALL_CS' and loc='*' ")
        sql("update localization set text='Cause of Death' where code='app.caseList.CD_CODD_REPTD_ALL' and loc='*' ")
        sql("update localization set text='Death ?' where code='app.caseList.FLAG_SERIOUS_DEATH' and loc='*' ")
        sql("update localization set text='Dose Details' where code='app.caseList.DOSE_DETAIL_ALL' and loc='*' ")
        sql("update localization set text='Patient Hist Drugs' where code='app.caseList.CODED_DRUG_NAME_ALL' and loc='*' ")
        sql("update localization set text='Patient Med Hist' where code='app.caseList.PAT_MED_COND_ALL' and loc='*' ")
        sql("update localization set text='Primary IND#' where code='app.caseList.STUDY_NUMBER' and loc='*' ")
        sql("update localization set text='Protocol#' where code='app.caseList.PROJECT_NUM' and loc='*' ")
        sql("update localization set text='Susar ?' where code='app.caseList.STATE_YN_5' and loc='*' ")
        sql("update localization set text='Therapy Dates' where code='app.caseList.THERAPY_DATES_ALL' and loc='*' ")
    }

    changeSet(author: "anurag", id: "202105271332-1") {
        sql("update localization set text='Date & Time should be in future to update ETL' where code='update.start.date.time.etl' and loc='*' ")
        sql("update localization set text='Date & Time should be in future to update ETL' where code='update.start.date.time.etl' and loc='ja' ")
    }
  changeSet(author: "sergey", id: "202106031332-1") {
        sql("update localization set text='Observation -> Error Type' where code='app.actionPlan.groupping.observation_issue' and loc='*' ")
        sql("update localization set text='Observation -> Error Type' where code='app.actionPlan.groupping.observation_issue' and loc='ja' ")
        sql("update localization set text='Only' where code='app.actionPlan.only' and loc='*' ")
        sql("update localization set text='Only' where code='app.actionPlan.only' and loc='ja' ")
    }
    changeSet(author: "anurag", id: "202106091332-1") {
        sql("update localization set text='Number Of Submissions' where code='app.actionPlan.NumberSubmissionReports' and loc='*' ")
        sql("update localization set text='Number Of Submissions' where code='app.actionPlan.NumberSubmissionReports' and loc='ja' ")
    }
    changeSet(author: "sergey", id: "202106231332-1") {
        sql("update localization set text='Last Interval' where code='app.actionPlan.LastPeriod' and loc='*' ")
        sql("update localization set text='Previous Interval' where code='app.actionPlan.PreviousPeriod' and loc='*' ")
        sql("update localization set text='Summaries For Selected Interval' where code='app.actionPlan.sumummaryFroPeriod' and loc='*' ")
        sql("update localization set text='Create New For Selected Interval' where code='app.actionPlan.createNewSP' and loc='*' ")
        sql("update localization set text='Compare to previous interval' where code='app.actionPlan.cpp' and loc='*' ")

    }
    changeSet(author: "ShubhamRx", id: "20210730051040-1") {
        sql("update localization set text='Case Type' where code='app.caseList.FLAG_ELIGIBLE_LOCAL_EXPDTD' and loc='*' ")
    }

    changeSet(author: "ShubhamRx", id: "20210817055959-1") {
        sql("update localization set text='All Suspect Product Active Ingredients' where code='app.caseList.PAI_ALL' and loc='*' ")
        sql("update localization set text='Positive Rechallenge' where code='app.caseList.RECHALL_DESC' and loc='*' ")
        sql("update localization set text='Malfunction ?' where code='app.caseList.MALFUNCTION' and loc='*' ")
    }

    changeSet(author: "ShubhamRx", id: "20210824123541-1") {
        sql("update localization set text='Latest FDA Receipt Date' where code='app.caseList.DATE_LATEST_SIG_FU_REPT' and loc='*' ")
        sql("update localization set text='Combination Product Flag' where code='app.caseList.FLAG_COMBINATION_PRODUCT' and loc='*' ")
        sql("update localization set text='Derived Country' where code='app.caseList.DERIVED_COUNTRY' and loc='*' ")
    }

    changeSet(author: "ShubhamRx", id: "20210831051929-1") {
        sql("update localization set text='Initial FDA Received Date' where code='app.caseList.DATE_FIRST_RECEIPT' and loc='*' ")
        sql("update localization set text='Primary Product Name/Study Drug (Coded)' where code='app.caseList.PRODUCT_NAME' and loc='*' ")
        sql("update localization set text='Primary Product Name' where code='app.caseList.PROD_NAME_RESOLVED' and loc='*' ")
        sql("update localization set text='Primary Reported Product Name' where code='app.caseList.PRIM_PROD_NAME' and loc='*' ")
        sql("update localization set text='Compounding Flag' where code='app.caseList.SENDER_OUTSOURCED' and loc='*' ")
    }

    changeSet(author: "RishabhJ", id: "202111101811-1") {
        sql("update localization set text='Send Action Item Email Notification On' where code='app.preference.actionItem.emails' and loc='*' ")
        sql("update localization set text='Send Report Request Email Notification On' where code='app.preference.reportRequest.emails' and loc='*' ")
        sql("update localization set text='Due Soon/ Overdue' where code='app.preference.actionItem.reminder.emails' and loc='*' ")
    }

    changeSet(author: "RishabhJ", id: "202111251344-1") {
        sql("update localization set text='Parameter Label/Question for \"{3}\" exceeds the maximum size of {4}.' where code='com.rxlogix.config.publisher.PublisherTemplateParameter.title.maxSize.exceeded' and loc='*' ")
        sql("update localization set text='Description for \"{3}\" exceeds the maximum size of {4}.' where code='com.rxlogix.config.publisher.PublisherTemplateParameter.description.maxSize.exceeded' and loc='*' ")
        sql("update localization set text='Evaluated/Default Value for \"{3}\" exceeds the maximum size of {4}.' where code='com.rxlogix.config.publisher.PublisherTemplateParameter.value.maxSize.exceeded' and loc='*' ")
    }

    changeSet(author: "RishabhJ", id: "202112091601-1") {
        sql("update localization set text='Publisher Template Library' where code='app.label.PublisherTemplate.templateLeftMenu' and loc='*' ")
    }

    changeSet(author: "RishabhJ", id: "202112211830-1") {
        sql("delete from localization where code='com.rxlogix.config.publisher.PublisherTemplateParameter.name.invalid.special.characters' ")
        sql("update localization set text='PV Reports - Publisher Template Library' where code='app.PublisherTemplate.list.title' and loc='*' ")
        sql("update localization set text='PV Reports - View Publisher Template' where code='app.PublisherTemplate.show.title' and loc='*' ")
    }
    
    changeSet(author: "Nitin Nepalia", id: "202112231905-1") {
        sql("update localization set text='Attachment Classification' where code='app.unitConfiguration.label.attachment' and loc='*' ")
    }
    changeSet(author: "Nitin Nepalia", id: "202201180100-1") {
        sql("update localization set text='Due in day(s) must be greater than 0, Please input valid number.' where code='com.rxlogix.config.configuration.dueInDays.positiveNumber' and loc='*' ")
    }
    changeSet(author: "Nitin Nepalia", id: "202201311730-1") {
        sql("update localization set text='Save as simple xml' where code='save.as.e2b.r2.xml' and loc='*' ")
        sql("update localization set text='Save as E2B(R2/R3) xml' where code='save.as.e2b.r3.xml' and loc='*' ")
    }

    changeSet(author: "Sachin Verma", id: "202202181730-1") {
        sql("update localization set text='Report Form' where code='icsr.profile.manual.schedule.templateQuery' and loc='*' ")
        sql("update localization set text='Report Form' where code='icsr.profile.manual.schedule.templateQuery' and loc='ja' ")
    }

    changeSet(author: "Rishabh Jain", id: "202202231330-1") {
        sql("update localization set text='一括更新' where code='app.menu.bulkScheduling' and loc='ja' ")
        sql("update localization set text='送信履歴のインポート' where code='app.pvc.import.submission' and loc='ja' ")
        sql("update localization set text='トランジションを' where code='app.label.workflow.rule.autoExecuteInDays_strt' and loc='ja' ")
        sql("update localization set text='日後に自動的に実行' where code='app.label.workflow.rule.autoExecuteInDays_end' and loc='ja' ")
        sql("update localization set text='カンマ区切り' where code='app.template.CommaSeparatedValue' and loc='ja' ")
        sql("update localization set text='全クーロス' where code='app.label.PublisherTemplate.ai.allclosed' and loc='ja' ")
    }

    changeSet(author: "Shubham Sharma", id: "202202230102-1") {
        sql("update localization set text='Due In (Days)' where code='icsr.profile.manual.schedule.dueInDays' and loc='*' ")
    }


    changeSet(author: "Shubham Sharma", id: "202203020502-1") {
        sql("update localization set text='新規の製品グループ' where code='app.button.product.group.create.label' and loc='ja' ")
        sql("update localization set text='新規の有害事象グループ' where code='app.label.new.event.group' and loc='ja' ")
    }

    changeSet(author: "Sergey", id: "202203140502-1") {
        sql("update localization set text='Advanced Settings Editor' where code='app.role.ROLE_CUSTOM_EXPRESSION'")
    }

    changeSet(author: "anurag", id: "202203221327-1") {
        sql("update localization set text='Generating ICSR preview for Case#{0}. Please refresh to check the latest execution status.' where code='icsr.generate.manual.request.success'")
        sql("update localization set text='Re-evaluating Case#{0} against scheduled profile(s)' where code='icsr.reEvaluate.manual.case.success'")
        sql("update localization set text='Case#{0} : Scheduled an ICSR Report for {1} profile.' where code='icsr.add.manual.case.success'")
        sql("update localization set text='Currently application is processing ICSR for other cases, Please try again after some time.' where code='icsr.generate.manual.no.slot'")
        sql("update localization set text='ICSR Tracking' where code='app.label.view.cases'")
    }

    changeSet(author: "Vinay", id: "202203221327-2") {
        sql("update localization set text='ICSR Tracking' where code='iscr.case.tracking.label'")
        sql("update localization set text='PV Reports - ICSR Tracking Management' where code='app.icsr.case.tracking.title'")
        sql("update localization set text='ICSR Tracking' where code='app.label.icsr.case.tracking'")
    }

    changeSet(author: "Rishabh", id: "202204071257-2") {
        sql("update localization set text='As of Specific Date' where code='app.reassessListednessEnum.CUSTOM_START_DATE'")
    }

    changeSet(author: 'ShubhamRx', id: "20220513123656-1") {
        sql("update localization set text='Product Name (Approval Number)' where code='icsr.case.tracking.productName'")
    }

    changeSet(author: "Meenal" , id: "202205171740-1") {
        sql("update localization set text='Product' where code='icsr.case.tracking.productName'")
        sql("update localization set text='Event PT' where code='icsr.case.tracking.eventPreferredTerm'")
        sql("update localization set text='Form' where code='icsr.case.tracking.reportForm'")
        sql("update localization set text='PV Case Management' where code='app.label.pvintake'")
    }


    changeSet(author: "Meenal" , id: "202205121716-1") {
        sql("update localization set text='Failed to update Workflow State, either you may not have rights to select the target state or the selected state is not applicable for some of the cases.' where code='app.periodicReportConfiguration.state.update.warn'")
    }
    changeSet(author: "sergey" , id: "202206221716-1") {
        sql("update localization set text='Due Date To HA' where code='app.label.reportRequest.currentPeriodDueDateToHa'")
        sql("update localization set text='Due In Post DLP (in days)' where code='app.label.reportRequest.dueDateToHa'")
        sql("update localization set text='Due In Post DLP' where code='app.label.reportRequest.dueDateToHa2'")
    }

    changeSet(author: "Ashish Dhami" , id:"202207171740-1"){
        sql("update localization set text='PVパブリッシャー' where code='app.label.pvpublisher' and loc='ja' ")
    }

    changeSet(author: "anurag (generated)", id: "202208131657-1") {
        sql("update localization set text='Auto Schedule' where code='app.label.icsr.profile.conf.autoScheduling' and loc='*' ")
        sql("update localization set text='Auto Schedule' where code='app.label.icsr.profile.conf.autoScheduling' and loc='ja' ")
    }
    changeSet(author: 'pragyatiwari', id: "060920221218-1"){
        sql("update localization set text='PV Publisher - Publisher Template Library' where code='app.PublisherTemplate.list.title' and loc='*' ")
    }

    changeSet(author: "anurag", id: "120920221539-1") {
        sql("update localization set text='PV Publisher - Create Publisher Template' where code='app.PublisherTemplate.create.title' and loc='*' ")
        sql("update localization set text='PV Publisher - View Publisher Template' where code='app.PublisherTemplate.show.title' and loc='*' ")
        sql("update localization set text='PV Publisher - Edit Publisher Template' where code='app.PublisherTemplate.edit.title' and loc='*' ")
        sql("update localization set text='PV Publisher - Create Plan Template' where code='app.gantt.create.title' and loc='*' ")
        sql("update localization set text='PV Publisher - View Plan Template' where code='app.gantt.show.title' and loc='*' ")
        sql("update localization set text='PV Publisher - Edit Plan Template' where code='app.gantt.edit.title' and loc='*' ")
    }

    changeSet(author: "Ashish Dhami" , id:"041020221457-1"){
        sql("update localization set text='定期報告プランニング' where code='app.label.topnav.report.request.plan' and loc='ja' ")
    }
    changeSet(author: "Seregy" , id:"202210101010-1"){
        sql("update localization set text='Report Task Template' where code='app.TaskTemplateTypeEnum.AGGREGATE_REPORTS' and loc='*' ")
        sql("update localization set text='Report Task Template' where code='app.TaskTemplateTypeEnum.AGGREGATE_REPORTS' and loc='ja' ")
    }
    changeSet(author: "Seregy" , id:"202210281010-1"){
        sql("update localization set text='QBE Report Editor' where code='app.role.ROLE_BQA_EDITOR' and loc='*' ")
        sql("update localization set text='QBE Report Editor' where code='app.role.ROLE_BQA_EDITOR' and loc='ja' ")
    }
  changeSet(author: "Seregy" , id:"202210281010-2"){
        sql("update localization set text='\"Report Due Date Past DLP\" is applicable for aggregate reports only, using it in adhoc reports will replace \"Report Due Date Past DLP\" with \"Creation Date\" automatically.' where code='app.Task.BaseDate.warn' ")
    }

    changeSet(author: 'meenal(generated)', id: "202230111624-1"){
        sql("update localization set text='Multiple Attachments are not allowed. Please Select Single Attachment at a time.' where code='quality.multi.select.attachment' and loc='*' ")
        sql("update localization set text='Multiple Attachments are not allowed. Please Select Single Attachment at a time.' where code='quality.multi.select.attachment' and loc='ja' ")
    }

    changeSet(author: 'anurag(generated)', id: "202212051037-1"){
        sql("update localization set text='ICSR Report' where code='app.workflowConfigurationType.ICSR_REPORT' and loc='*' ")
        sql("update localization set text='ICSR Report' where code='app.workflowConfigurationType.ICSR_REPORT' and loc='ja' ")
    }

    changeSet(author: 'ashishdhami', id: "202208121818-1"){
        sql("update localization set text='Generate Data Analysis File' where code='app.spotfire.caseSeries.generate.spotfire' and loc='*' ")
        sql("update localization set text='Generate Data Analysis File' where code='app.spotfire.caseSeries.generate.spotfire' and loc='ja' ")
    }

    changeSet(author: "Riya" , id:"202304051158-2"){
        sql("update localization set text='Inbound Compliance Initialization is In Progress' where code='app.sender.initialize.triggered.msg' and loc='*' ")
        sql("update localization set text='Inbound Compliance Initialization is In Progress' where code='app.sender.initialize.triggered.msg' and loc='ja' ")
    }

    changeSet(author: 'rishabh', id: "202302271538-1"){
        sql("update localization set text='PVC Outbound Viewer' where code='app.role.ROLE_PVC_EDIT'")
        sql("update localization set text='PVC Outbound Editor' where code='app.role.ROLE_PVC_VIEW'")
    }
    changeSet(author: 'rishabh', id: "202302271538-2"){
        sql("update localization set text='PVC Inbound Viewer' where code='app.role.ROLE_PVC_INBOUND_VIEW'")
    }

    changeSet(author: 'sergey', id: "2023030171538-1"){
        sql("update localization set text='Please select site and folder in Sharepoint' where text='Please select site and folded in Sharepoint'")
        sql("update localization set text='Help message for label {0} successfully deleted!' where text='Help message for label ''{0}'' successfully deleted!'")
        sql("update localization set text='Help message for label {0} successfully deleted!' where text='Help message for label ''{1}'' successfully deleted!'")
    }
    changeSet(author: 'sergey', id: "2023032471538-1"){
        sql("update localization set text='What''s New' where text='Release Notes'")
        sql("update localization set text='What''s New' where text='Release Note'")
        sql("update localization set text='What''s New Item {1} successfully deleted!' where text='Release Note Item ''{1}'' successfully deleted!'")
        sql("update localization set text='What''s New record {1} successfully deleted' where text='Release Note ''{1}'' successfully deleted'")
        sql("update localization set text='PV Reports - What''s New List' where text='PV Reports - Release Notes List'")
        sql("update localization set text='No What''s New Messages' where text='No Release Notes'")
        sql("update localization set text='PV Reports - Add What''s New' where text='PV Reports - Creat Release Note'")
        sql("update localization set text='Add What''s New' where text='Create Release Note'")
        sql("update localization set text='PV Reports - Edit What''s New' where text='PV Reports - Edit Release Note'")
        sql("update localization set text='PV Reports - View What''s New' where text='=PV Reports - View Release Note'")
        sql("update localization set text='Edit What''s New' where text='Edit Release Note'")
        sql("update localization set text='Create New Item' where text='Create New Release Note Item'")
    }
    changeSet(author: 'meenal', id: "202302241450-1"){
        sql("update localization set text='What''s New record' where code='app.label.localizationHelp.releaseNote.delete' and loc='*' ")
        sql("update localization set text='What''s New Item' where code='app.label.localizationHelp.releaseNoteItem.delete' and loc='*' ")
    }
   changeSet(author: 'seergey', id: "202304181450-2"){
        sql("update localization set text='Document Calendar' where code='app.aggregate.report.calendar'")
        sql("update localization set text='PV Publisher - Document Calendar Calendar' where code='app.aggregate.report.calendar.title'")
        sql("update localization set text='Document Planning' where code='app.label.topnav.report.request.plan' ")
        sql("update localization set text='Show on Document Planning page' where code='app.label.reportRequestType.showInPlan'")
        sql("update localization set text='Document Planning Viewer' where code='app.role.ROLE_REPORT_REQUEST_PLAN_VIEW'")
        sql("update localization set text='Document Planning Team' where code='app.role.ROLE_REPORT_REQUEST_PLANNING_TEAM'")
    }

    changeSet(author: 'riya', id: "202306091229-1"){
        sql("update localization set text='ICSR Profile' where code='app.executionStatus.configType.ICSR_PROFILE'")
    }

    changeSet(author: 'anurag', id: "202307311524-1"){
        sql("update localization set text='ETL Status' where code='app.etlStatus.label' and loc='*'")
        sql("update localization set text='ETL Status' where code='app.etlStatus.label' and loc='ja' ")
    }

    changeSet(author: 'meenal', id: "202308251246-1"){
        sql("update localization set text='External Folder' where code='app.distributionChannel.OTHER_GATEWAY' and loc='*' ")
    }

    changeSet(author: "anurag", id: "202309280833-1") {
        sql("delete from localization where code='com.rxlogix.jobs.BalanceMinusQueryJob' ")
    }

    changeSet(author: 'sergey', id: "202309261246-1"){
        sql("update localization set text='Redacted' where code='app.template.protected'")
    }

    changeSet(author: 'gunjan' , id: "202310101539-1"){
        sql("update localization set text='ETL has been resumed manually using Resume ETL Now' where code='app.etl.resume.message'")
    }

    changeSet(author: 'shivam' , id: "202310261142-1"){
        sql("update localization set text='Report is too large to be attached. Go to PV Reports to view report.' where code='app.emailService.result.nourl.message.label'")
    }

    changeSet(author: 'seergey', id: "202305151450-2"){
        sql("update localization set text='No' where code='app.percentageOptionEnum.NO_PERCENTAGE'")
        sql("update localization set text='By Total' where code='app.percentageOptionEnum.BY_TOTAL'")
        sql("update localization set text='By Subtotal' where code='app.percentageOptionEnum.BY_SUBTOTAL' ")
    }

    changeSet(author: 'shivam' , id: "202311061700-1"){
        sql("update localization set text='Need Approval' where code='app.label.workflow.rule.needApproval'")

    }

    changeSet(author: 'seergey', id: "202301051450-2"){
        sql("update localization set text='Previous periods' where code='app.actionPlan.previousPeriods'  and loc='*'")
    }

    changeSet(author: "shivam", id: "202311211221-1"){
        sql("update localization set text='Deleted ICSR Report for {0} (v{1}) - {2} : ({3}) Recipient : {4} (Justification : {5})' where code='auditLog.entityValue.icsr.delete'")
    }

    changeSet(author: "vivek", id: "202312081430-1"){
        sql("update localization set text='Please enter login password to approve the reports for distribution' where code='app.label.workflow.rule.needApproval.transmission'")
        sql("update localization set text='Please enter login password to approve the reports for submission' where code='app.label.workflow.rule.needApproval.submission'")
    }

    changeSet(author: "vivek", id: "202312121238-1"){
        sql("update localization set text='Transmitting Attachment' where code='app.icsrCaseState.TRANSMITTING_ATTACHMENT'")
    }

    changeSet(author: "anurag", id: "202312181614-1"){
        sql("update localization set text='Auto-Transmitting ICSR Report' where code='app.auto.transmit.comment'")
        sql("update localization set text='Auto-Transmitting linked attachment file' where code='app.auto.transmit.attachment.comment'")
    }

    changeSet(author: "Vivek", id: "202401301625-1") {
        sql("update localization set text='ICSR Organization Configuration' where code='app.unitConfiguration.label.myInbox'")
    }

    changeSet(author: "meenal", id: "202401011308-1") {
        sql("update localization set text='PV Reports - RCA Mapping' where code='app.lateMapping.title'")
    }

    changeSet(author: "shivam", id: "202402271246-1") {
        sql("update localization set text='Validation Framework' where code='app.balanceMinusQuery.label'")
        sql("update localization set text='PV Reports - Current Validation Framework Status' where code='app.balanceMinusQueryStatus.title'")
    }

    changeSet(author: 'RxL-Eugen-Semenov', id: "202403211411-1"){
        sql("update localization set text='Last Run Status' where code='app.configuration.autorca.lastRunStatus' and loc='*'")
        sql("update localization set text='Last Run Status' where code='app.configuration.autorca.lastRunStatus' and loc='ja' ")
    }

    changeSet(author: "Pragya-Tiwari", id: "202405071657-1"){
        sql("update localization set text='The queue for bulk downloading ICSR reports is currently full. Please try again later.' where code='icsr.reports.bulk.download.configuration.size.exceed.error' and loc='*'")
        sql("update localization set text='Number of reports selected for bulk download exceeded allowed limit ({0}).' where code='icsr.reports.bulk.download.report.count.exceed.error' and loc='*'")
    }
    changeSet(author: "gunjan", id: "202405231630-1") {
        sql("update localization set text='ICSR PADER Case' where code='app.queryDropdown.ICSR_PADER_AGENCY_CASES'")
        sql("update localization set text='ICSR PADER Case Query already exists' where code='app.query.icsrPadderAgencyCases.count'")
    }

    changeSet(author: "sergey", id: "202405291630-1") {
        sql("update localization set text='Select a Query' where code='app.label.chooseAQuery' and loc='*'")
        sql("update localization set text='Select a Template' where code='app.label.chooseAReportTemplate' and loc='*'")
        sql("update localization set text='Evaluate Case Data as Of' where code='app.label.EvaluateCaseDateOn' and loc='*'")
        sql("update localization set text='Create New Report' where code='app.newReport.menu' and loc='*'")
        sql("update localization set text='None (Run Once)' where code='scheduler.none.run.once' and loc='*'")
        sql("update localization set text='Due In Past DLP (Days) cannot exceed 365 days' where code='com.rxlogix.config.PeriodicReportConfiguration.dueInDays.max.exceeded'")
        sql("update localization set text='Due In Past DLP (Days) cannot be less than 0' where code='com.rxlogix.config.PeriodicReportConfiguration.dueInDays.min.notmet' ")
        sql("update localization set text='Due In Past DLP (Days)' where code='app.label.dueInDaysPastDLP'")
    }

    changeSet(author: "vivek", id: "202406111615-1") {
        sql("update localization set text='ICSR追跡' where code='app.label.view.cases' and loc='ja'")
        sql("update localization set text='ICSR追跡' where code='iscr.case.tracking.label' and loc='ja'")
        sql("update localization set text='ICSR追跡' where code='app.label.icsr.case.tracking' and loc='ja'")
        sql("update localization set text='アクセス管理' where code='app.label.accessManagement' and loc='ja'")
        sql("update localization set text='ビジネスコンフィグ' where code='app.label.settings.business' and loc='ja'")
        sql("update localization set text='システムコンフィグ' where code='app.label.settings.system' and loc='ja'")
        sql("update localization set text='ユーティリティ' where code='app.label.settings.util' and loc='ja'")
        sql("update localization set text='管理ツール' where code='app.label.settings.tools' and loc='ja'")
        sql("update localization set text='PV部門入手日' where code='app.label.safety.receipt.date' and loc='ja'")
        sql("update localization set text='製品' where code='icsr.case.tracking.productName' and loc='ja'")
        sql("update localization set text='送信エラー' where code='app.icsrCaseState.TRANSMISSION_ERROR' and loc='ja'")
        sql("update localization set text='添付ファイル送信中' where code='app.icsrCaseState.TRANSMITTING_ATTACHMENT' and loc='ja'")
        sql("update localization set text='添付ファイル送信済み' where code='app.icsrCaseState.TRANSMITTED_ATTACHMENT' and loc='ja'")
        sql("update localization set text='自動スケジュール' where code='app.label.icsr.profile.conf.autoScheduling' and loc='ja'")
        sql("update localization set text='期日調整' where code='app.label.adjust.due.date' and loc='ja'")
        sql("update localization set text='FUレポートの自動スケジュール' where code='app.label.icsr.profile.conf.autoScheduleFUPReport' and loc='ja'")
        sql("update localization set text='最終確定前にレポートを生成' where code='app.label.icsr.profile.conf.includeOpenCases' and loc='ja'")
        sql("update localization set text='ローカル症例評価が必要' where code='app.label.icsr.profile.conf.localCpRequired' and loc='ja'")
        sql("update localization set text='報告不要の症例を含む' where code='reportCriteria.include.non.reportable.cases' and loc='ja'")
        sql("update localization set text='製品' where code='app.product.name' and loc='ja'")
    }

    changeSet(author: "Pragya-Tiwari", id: "202405241720-9"){
        sql("update localization set text='Name or Description can not be blank.' where code='com.rxlogix.config.Email.description.nullable' and loc='*'")
        sql("update localization set text='Name or Description can not be blank.' where code='com.rxlogix.config.Email.description.blank' and loc='*'")
        sql("update localization set text='Email body can not be empty.' where code='com.rxlogix.config.EmailTemplate.body.nullable' and loc='*'")
        sql("update localization set text='Email body can not be empty.' where code='com.rxlogix.config.EmailTemplate.body.blank' and loc='*'")
    }

    changeSet(author: "gunjan", id: "202407241821"){
        sql("update localization set text='Criteria Name and Query is required' where code='com.rxlogix.config.InboundCompliance.queriesCompliance.minSize.notmet' and loc='*'")

    }
    changeSet(author: "Pragya-Tiwari", id: "202407241719-1"){
        sql("update localization set text='Name is required' where code IN ('com.rxlogix.commandObjects.CustomReportFieldCO.customName.nullable','com.rxlogix.config.CustomReportField.customName.nullable') and loc='*'")
        sql("update localization set text='Custom Expression is required' where code IN ('com.rxlogix.commandObjects.CustomReportFieldCO.defaultExpression.nullable','com.rxlogix.config.CustomReportField.defaultExpression.nullable') and loc='*'")
        sql("delete from localization where code IN ('com.rxlogix.commandObjects.CustomReportFieldCO.reportFieldId.typeMismatch.error') and loc='*'")
    }

    changeSet(author: "meenal", id: "202407301914-1"){
        sql("update localization set text='PV Reports - Import Configuration' where code='app.PeriodicReport.bulkScheduling.title'")
    }

    changeSet(author: "meenal", id: "202407311237-1"){
        sql("update localization set text='Central Receipt Date' where code='icsr.case.tracking.safetyReceiptDate' and loc='*'")
    }

    changeSet(author: "sergey", id: "202408061237-1"){
        sql("update localization set text='You have selected the maximum of 500 rows for batch editing. If you need to make changes to more than 500 rows, please do so in multiple steps, each with no more than 500 rows.' where code='app.reasonOfDelay.bulkUpdateMaxRowsWarning' ")
    }

    changeSet(author: "sergey", id: "202408061237-2"){
        sql("update localization set text='Same Case Submissions' where code='app.preference.ROD.similar.cases' ")
    }

    changeSet(author: "gunjan", id: "202408201240") {
        sql("""update localization set text='Avoid using these special characters (#;''<">) in label field' where code='app.pvc.label.validation.note' and loc='*'""")
        sql("""update localization set text='Avoid using these special characters (#;''<">) in quality issue description' 
    where code='app.pvq.qualityIssueDescription.validation.note' and loc='*'""")
        sql("""update localization set text='Avoid using these special characters (#;''<">) in quality observation details' 
    where code='app.pvc.label.validation.note' and loc='*'""")
        sql("update localization set text='Warn : No such template. May be deleted.' where code='app.template.warn.isDeleted' and loc='*'")
    }

    changeSet(author: "meenal", id: "202409021634-1"){
        sql("update localization set text='Create Issue' where code='app.label.quality.create.report.issue'")
        sql("update localization set text='PV Quality - Create Issue' where code='app.quality.title.quality.issue.create'")
        sql("update localization set text='PV Central - Create Issue' where code='app.central.title.central.issue.create'")
    }

    changeSet(author: "vivek", id: "202407301356-1") {
        sql("update localization set text='無効化' where code='app.label.icsr.profile.conf.disabled' and loc='ja'")
        sql("update localization set text='自動生成' where code='app.label.icsr.profile.conf.autoGenerate' and loc='ja'")
    }

    changeSet(author: "Shubham", id: "202409171756-1") {
        sql("update localization set text='Product' where code='icsr.profile.manual.schedule.Device'")
    }

    changeSet(author: "Vivek", id: "202409261420-1") {
        sql("update localization set text='緊急報告？' where code='icsr.profile.manual.schedule.expedited' and loc='ja'")
    }

    changeSet(author: "Shivam", id: "202410141234-1") {
        sql("update localization set text='個別症例安全性報告機構設定' where code='app.unitConfiguration.label.myInbox' and loc='ja'")
    }

    changeSet(author: "Siddharth", id: "20241003034531-7") {
        sql("update localization set text='Reports' where code='app.queryTarget.REPORTS' and loc='*'")
    }

    changeSet(author: "Shivam", id: "202410161541-1") {
        sql("update localization set text='(GMT {0}) 国際日付変更線 西' where code='app.timezone.TZ_0' and loc='ja'")
        sql("update localization set text='(GMT {0}) ミッドウェー島、サモア' where code='app.timezone.TZ_1' and loc='ja'")
        sql("update localization set text='(GMT {0}) ハワイ' where code='app.timezone.TZ_2' and loc='ja'")
        sql("update localization set text='(GMT {0}) アラスカ' where code='app.timezone.TZ_3' and loc='ja'")
        sql("update localization set text='(GMT {0}) 太平洋時間 (米国およびカナダ); ティファナ' where code='app.timezone.TZ_4' and loc='ja'")
        sql("update localization set text='(GMT {0}) 山岳地帯時間 (米国およびカナダ)' where code='app.timezone.TZ_10' and loc='ja'")
        sql("update localization set text='(GMT {0}) チワワ、ラパス、マサトラン' where code='app.timezone.TZ_13' and loc='ja'")
        sql("update localization set text='(GMT {0}) アリゾナ' where code='app.timezone.TZ_15' and loc='ja'")
        sql("update localization set text='(GMT {0}) 中部時間 (米国およびカナダ)' where code='app.timezone.TZ_20' and loc='ja'")
        sql("update localization set text='(GMT {0}) サスカチュワン州' where code='app.timezone.TZ_25' and loc='ja'")
        sql("update localization set text='(GMT {0}) グアダラハラ、メキシコシティ、モンテレー' where code='app.timezone.TZ_30' and loc='ja'")
        sql("update localization set text='(GMT {0}) 中央アメリカ' where code='app.timezone.TZ_33' and loc='ja'")
        sql("update localization set text='(GMT {0}) 東部時間 (米国およびカナダ)' where code='app.timezone.TZ_35' and loc='ja'")
        sql("update localization set text='(GMT {0}) インディアナ州 (東部)' where code='app.timezone.TZ_40' and loc='ja'")
        sql("update localization set text='(GMT {0}) ボゴタ、リマ、キト' where code='app.timezone.TZ_45' and loc='ja'")
        sql("update localization set text='(GMT {0}) 大西洋時間 (カナダ)' where code='app.timezone.TZ_50' and loc='ja'")
        sql("update localization set text='(GMT {0}) カラカス、ラパス' where code='app.timezone.TZ_55' and loc='ja'")
        sql("update localization set text='(GMT {0}) サンティアゴ' where code='app.timezone.TZ_56' and loc='ja'")
        sql("update localization set text='(GMT {0}) ニューファンドランドおよびラブラドール州' where code='app.timezone.TZ_60' and loc='ja'")
        sql("update localization set text='(GMT {0}) ブラジリア' where code='app.timezone.TZ_65' and loc='ja'")
        sql("update localization set text='(GMT {0}) ブエノスアイレス、ジョージタウン' where code='app.timezone.TZ_70' and loc='ja'")
        sql("update localization set text='(GMT {0}) グリーンランド' where code='app.timezone.TZ_73' and loc='ja'")
        sql("update localization set text='(GMT {0}) 中部大西洋' where code='app.timezone.TZ_75' and loc='ja'")
        sql("update localization set text='(GMT {0}) アゾレス諸島' where code='app.timezone.TZ_80' and loc='ja'")
        sql("update localization set text='(GMT {0}) カーボベルデ諸島' where code='app.timezone.TZ_83' and loc='ja'")
        sql("update localization set text='(GMT {0}) グリニッジ標準時: ダブリン、エディンバラ、リスボン、ロンドン' where code='app.timezone.TZ_85' and loc='ja'")
        sql("update localization set text='(GMT {0}) モンロビア、カサブランカ' where code='app.timezone.TZ_90' and loc='ja'")
        sql("update localization set text='(GMT {0}) ベオグラード、ブラチスラヴァ、ブダペスト、リュブリャナ、プラハ' where code='app.timezone.TZ_95' and loc='ja'")
        sql("update localization set text='(GMT {0}) サラエボ、スコピエ、ワルシャワ、ザグレブ' where code='app.timezone.TZ_100' and loc='ja'")
        sql("update localization set text='(GMT {0}) ブリュッセル、コペンハーゲン、マドリッド、パリ' where code='app.timezone.TZ_105' and loc='ja'")
        sql("update localization set text='(GMT {0}) アムステルダム、ベルリン、ベルン、ローマ、ストックホルム、ウィーン' where code='app.timezone.TZ_110' and loc='ja'")
        sql("update localization set text='(GMT {0}) 西中央アフリカ' where code='app.timezone.TZ_113' and loc='ja'")
        sql("update localization set text='(GMT {0}) ブカレスト' where code='app.timezone.TZ_115' and loc='ja'")
        sql("update localization set text='(GMT {0}) カイロ' where code='app.timezone.TZ_120' and loc='ja'")
        sql("update localization set text='(GMT {0}) ヘルシンキ、キエフ、リガ、ソフィア、タリン、ビリニュス' where code='app.timezone.TZ_125' and loc='ja'")
        sql("update localization set text='(GMT {0}) アテネ、イスタンブール、ミンスク' where code='app.timezone.TZ_130' and loc='ja'")
        sql("update localization set text='(GMT {0}) エルサレム' where code='app.timezone.TZ_135' and loc='ja'")
        sql("update localization set text='(GMT {0}) プレトリア州ハラレ' where code='app.timezone.TZ_140' and loc='ja'")
        sql("update localization set text='(GMT {0}) モスクワ、サンクトペテルブルク、ヴォルゴグラード' where code='app.timezone.TZ_145' and loc='ja'")
        sql("update localization set text='(GMT {0}) クウェート、リヤド' where code='app.timezone.TZ_150' and loc='ja'")
        sql("update localization set text='(GMT {0}) ナイロビ' where code='app.timezone.TZ_155' and loc='ja'")
        sql("update localization set text='(GMT {0}) バグダッド' where code='app.timezone.TZ_158' and loc='ja'")
        sql("update localization set text='(GMT {0}) テヘラン' where code='app.timezone.TZ_160' and loc='ja'")
        sql("update localization set text='(GMT {0}) アブダビ、マスカット' where code='app.timezone.TZ_165' and loc='ja'")
        sql("update localization set text='(GMT {0}) バクー、トビリシ、エレバン' where code='app.timezone.TZ_170' and loc='ja'")
        sql("update localization set text='(GMT {0}) カブール' where code='app.timezone.TZ_175' and loc='ja'")
        sql("update localization set text='(GMT {0}) エカテリンブルク' where code='app.timezone.TZ_180' and loc='ja'")
        sql("update localization set text='(GMT {0}) イスラマバード、カラチ、タシケント' where code='app.timezone.TZ_185' and loc='ja'")
        sql("update localization set text='(GMT {0}) チェンナイ、コルカタ、ムンバイ、ニューデリー' where code='app.timezone.TZ_190' and loc='ja'")
        sql("update localization set text='(GMT {0}) カトマンズ' where code='app.timezone.TZ_193' and loc='ja'")
        sql("update localization set text='(GMT {0}) アスタナ、ダッカ' where code='app.timezone.TZ_195' and loc='ja'")
        sql("update localization set text='(GMT {0}) スリ ジャヤワル ダナプラ' where code='app.timezone.TZ_200' and loc='ja'")
        sql("update localization set text='(GMT {0}) アルマトイ、ノボシビルスク' where code='app.timezone.TZ_201' and loc='ja'")
        sql("update localization set text='(GMT {0}) ヤンゴン ラングーン' where code='app.timezone.TZ_203' and loc='ja'")
        sql("update localization set text='(GMT {0}) バンコク、ハノイ、ジャカルタ' where code='app.timezone.TZ_205' and loc='ja'")
        sql("update localization set text='(GMT {0}) クラスノヤルスク' where code='app.timezone.TZ_207' and loc='ja'")
        sql("update localization set text='(GMT {0}) 北京、重慶、香港特別行政区、ウルムチ' where code='app.timezone.TZ_210' and loc='ja'")
        sql("update localization set text='(GMT {0}) クアラルンプール、シンガポール' where code='app.timezone.TZ_215' and loc='ja'")
        sql("update localization set text='(GMT {0}) 台北' where code='app.timezone.TZ_220' and loc='ja'")
        sql("update localization set text='(GMT {0}) パース' where code='app.timezone.TZ_225' and loc='ja'")
        sql("update localization set text='(GMT {0}) イルクーツク、ウランバートル' where code='app.timezone.TZ_227' and loc='ja'")
        sql("update localization set text='(GMT {0}) ソウル' where code='app.timezone.TZ_230' and loc='ja'")
        sql("update localization set text='(GMT {0}) 大阪、札幌、東京' where code='app.timezone.TZ_235' and loc='ja'")
        sql("update localization set text='(GMT {0}) ヤクーツク' where code='app.timezone.TZ_240' and loc='ja'")
        sql("update localization set text='(GMT {0}) ダーウィン' where code='app.timezone.TZ_245' and loc='ja'")
        sql("update localization set text='(GMT {0}) アデレード' where code='app.timezone.TZ_250' and loc='ja'")
        sql("update localization set text='(GMT {0}) キャンベラ、メルボルン、シドニー' where code='app.timezone.TZ_255' and loc='ja'")
        sql("update localization set text='(GMT {0}) ブリスベン' where code='app.timezone.TZ_260' and loc='ja'")
        sql("update localization set text='(GMT {0}) ホバート' where code='app.timezone.TZ_265' and loc='ja'")
        sql("update localization set text='(GMT {0}) ウラジオストク' where code='app.timezone.TZ_270' and loc='ja'")
        sql("update localization set text='(GMT {0}) グアム、ポートモレスビー' where code='app.timezone.TZ_275' and loc='ja'")
        sql("update localization set text='(GMT {0}) ニューカレドニア、ソロモン諸島、マガダン' where code='app.timezone.TZ_280' and loc='ja'")
        sql("update localization set text='(GMT {0}) フィジー諸島、カムチャッカ、マーシャル諸島' where code='app.timezone.TZ_285' and loc='ja'")
        sql("update localization set text='(GMT {0}) オークランド、ウェリントン' where code='app.timezone.TZ_290' and loc='ja'")
        sql("update localization set text='(GMT {0}) ヌクアロファ' where code='app.timezone.TZ_300' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+11' where code='app.timezone.TZ_301' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+10' where code='app.timezone.TZ_302' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+9' where code='app.timezone.TZ_303' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+8' where code='app.timezone.TZ_304' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+7' where code='app.timezone.TZ_305' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+6' where code='app.timezone.TZ_306' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+5' where code='app.timezone.TZ_307' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+4' where code='app.timezone.TZ_308' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+3' where code='app.timezone.TZ_309' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+2' where code='app.timezone.TZ_310' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT+1' where code='app.timezone.TZ_311' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT' where code='app.timezone.TZ_312' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-1' where code='app.timezone.TZ_313' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-2' where code='app.timezone.TZ_314' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-3' where code='app.timezone.TZ_315' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-4' where code='app.timezone.TZ_316' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-5' where code='app.timezone.TZ_317' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-6' where code='app.timezone.TZ_318' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-7' where code='app.timezone.TZ_319' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-8' where code='app.timezone.TZ_320' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-9' where code='app.timezone.TZ_321' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-10' where code='app.timezone.TZ_322' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-11' where code='app.timezone.TZ_323' and loc='ja'")
        sql("update localization set text='(GMT {0}) など/GMT-12' where code='app.timezone.TZ_324' and loc='ja'")
    }
    changeSet(author: "Gunjan", id: "202410161812-1") {
        sql("update localization set text='PV Reports- 個別症例安全性報告' where code='app.icsrReports.title' and loc='ja'")
        sql("update localization set text='PV Reports - トレンドチャート' where code='quality.title.trending.chart' and loc='ja'")
        sql("update localization set text='PV Reports - 品質サンプリング' where code='quality.title.quality.sampling' and loc='ja'")
        sql("update localization set text='PV Reports - CAPA 8Dレポートの作成' where code='quality.title.quality.capa.create' and loc='ja'")
        sql("update localization set text='PV Reports - 問題レポートを編集' where code='quality.title.quality.issue.edit' and loc='ja'")
        sql("update localization set text='PV Reports - 問題レポートを表示' where code='quality.title.quality.issue.view' and loc='ja'")
        sql("update localization set text='PV Reports - 発行レポート一覧' where code='quality.title.quality.issue.list' and loc='ja'")
        sql("update localization set text='PV Reports - 提出品質' where code='quality.title.quality.submission.list' and loc='ja'")
        sql("update localization set text='PV Reports - ケースシリーズ' where code='app.caseSeries.title' and loc='ja'")
        sql("update localization set text='PV Reports - {0} テンプレート' where code='app.template.title' and loc='ja'")
        sql("update localization set text='PV Reports - クエリ' where code='app.query.title' and loc='ja'")
        sql("update localization set text='PV Reports - レポートリクエスト' where code='app.report.request.title' and loc='ja'")
        sql("update localization set text='PV Reports - ソースプロファイル' where code='app.sourceProfile.title' and loc='ja'")
        sql("update localization set text='PV Reports - ソースプロファイルの作成' where code='app.sourceProfile.create.title' and loc='ja'")
        sql("update localization set text='PV Reports - ソースプロファイルの編集' where code='app.sourceProfile.edit.title' and loc='ja'")
        sql("update localization set text='PV Reports - ソースプロファイルを表示' where code='app.sourceProfile.show.title' and loc='ja'")
        sql("update localization set text='PV Reports- 個別症例安全性報告' where code='app.icsrReport.title' and loc='ja'")
        sql("update localization set text='PV Reports - JSONｓをダウンロードする' where code='app.download.json.title' and loc='ja'")
        sql("update localization set text='PV Reports - {0}の作成' where code='default.create.title' and loc='ja'")
        sql("update localization set text='PV Reports - {0}の参照' where code='default.show.title' and loc='ja'")
        sql("update localization set text='PV Reports - {0}の編集' where code=' default.edit.title' and loc='ja'")
        sql("update localization set text='製品名' where code='icsr.profile.manual.schedule.Device' and loc='ja'")
        sql("update localization set text='製品タイプ' where code='icsr.profile.manual.authorization.type' and loc='ja'")
        sql("update localization set text='承認番号' where code='icsr.profile.manual.approval.number' and loc='ja'")
        sql("update localization set text='削除症例を除外する' where code='reportCriteria.exclude.deleted.cases' and loc='ja'")
        sql("update localization set text='症例シーリーズライブラリ' where code='caseSeries.library.label' and loc='ja'")
        sql("update localization set text='報告日(受信者のTZ）' where code='icsr.case.tracking.submissionDatePreferredTime' and loc='ja'")
        sql("update localization set text='報告書フォーム' where code='icsr.case.tracking.reportForm' and loc='ja'")
        sql("update localization set text='プロフィール' where code='icsr.case.tracking.profile' and loc='ja'")
        sql("update localization set text='スケジュール日' where code='icsr.case.tracking.scheduledDate' and loc='ja'")
        sql("update localization set text='旧バージョンを削除する' where code='app.label.removeOldVersion' and loc='ja'")
        sql("update localization set text='作成日' where code='app.Task.BaseDate.CREATION_DATE' and loc='ja'")
    }

    changeSet(author: "Vivek", id: "202411061716-1") {
        sql("update localization set text='レポート設定の取り込み' where code='app.menu.bulkImportConfiguration' and loc='ja'")
    }

    changeSet(author: "Gunjan", id: "202411081935-3") {
        sql("update localization set text='PV Central' where code='app.label.pvcentral' and loc='ja'")
        sql("update localization set text='PV Quality' where code='app.label.pv.quality' and loc='ja'")
        sql("update localization set text='PV Signal' where code='app.label.pvsignal' and loc='ja'")
        sql("update localization set text='PV Publisher' where code='app.label.pvpublisher' and loc='ja'")
    }

    changeSet(author: "Siddharth", id: "2024112060500-07") {
        sql("update localization set text='FUタイプ' where code='app.label.followUpInfo' and loc='ja'")
        sql("update localization set text='治験/市販後区分' where code='icsr.case.tracking.authorization.type' and loc='ja'")
        sql("update localization set text='作成エラー' where code='app.icsrCaseState.GENERATION_ERROR' and loc='ja'")
        sql("update localization set text='ステータス変更実施者' where code='app.label.icsr.case.history.routedBy' and loc='ja'")
        sql("update localization set text='コメント' where code='app.label.icsr.case.history.comment' and loc='ja'")
        sql("update localization set text='日付' where code='app.label.icsr.case.history.routed.date' and loc='ja'")
    }

    changeSet(author: "meenal", id: "202411141514-01") {
        sql("update localization set text='ICSRレポートの一括ダウンロードの処理中です。完了後に通知されます。' where code='icsr.reports.preapare.bulk.download.message' and loc='ja'")
        sql("update localization set text='ICSRレポートの一括ダウンロードの処理に失敗しました' where code='icsr.reports.prepare.bulk.download.error' and loc='ja'")
        sql("update localization set text='一括ダウンロードの許容値を超えています。時間をおいて再度実行してください' where code='icsr.reports.bulk.download.configuration.size.exceed.error' and loc='ja'")
        sql("update localization set text='一括ダウンロードの許容値を超えています。' where code='icsr.reports.bulk.download.report.count.exceed.error' and loc='ja'")
        sql("update localization set text='{0} 件のレポートの一括ダウンロードに成功しましたが、  {1} 件のダウンロードに失敗しました' where code='icsr.reports.bulk.download.message' and loc='ja'")
        sql("update localization set text='{0} 件の一括ダウンロードに成功しました' where code='icsr.reports.bulk.download.success.message' and loc='ja'")
        sql("update localization set text='一括ダウンロードに失敗しました' where code='icsr.report.download.error' and loc='ja'")
    }
  
    changeSet(author: "Siddharth", id: "202411141559-01") {
        sql("update localization set text='Company Identifier' where code='app.unitConfiguration.label.companyName'")
    }

    changeSet(author: "meenal", id: "202418111436-01") {
        sql("update localization set text='日本起算日を使用する' where code='app.label.icsr.profile.conf.japanAwareDate' and loc='ja'")
    }

    changeSet(author: "meenal", id: "202411191249-01") {
        sql("delete from localization where code='app.role.ROLE_ADMIN' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CONFIGURATION_CRUD' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CONFIGURATION_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_DATA_ANALYSIS' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_DEV' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_QUERY_ADVANCED' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_QUERY_CRUD' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_QUERY_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_TEMPLATE_ADVANCED' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_TEMPLATE_CRUD' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_TEMPLATE_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_TEMPLATE_SET_CRUD' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_TEMPLATE_SET_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_COGNOS_CRUD' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_COGNOS_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_QUALITY_CHECK' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_REPORT_REQUEST_CRUD' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_REPORT_REQUEST_PLAN_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_REPORT_REQUEST_PLANNING_TEAM' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_REPORT_REQUEST_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_REPORT_REQUEST_ASSIGN' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CASE_SERIES_EDIT' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CASE_SERIES_CRUD' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CASE_SERIES_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_ACTION_ITEM' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CALENDAR' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CHART_TEMPLATE_EDITOR' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_SHARE_GROUP' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_SHARE_ALL' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PVQ_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PVQ_EDIT' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PVC_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PVC_EDIT' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PVC_INBOUND_EDIT' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PVC_INBOUND_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CUSTOM_EXPRESSION' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CUSTOM_FIELD' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_CONFIG_TMPLT_CREATOR' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_ICSR_REPORTS_EDITOR' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_ICSR_REPORTS_VIEWER' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_ICSR_PROFILE_EDITOR' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_ICSR_PROFILE_VIEWER' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_USER_MANAGER' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_DMS' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_USER_GROUP_RCA' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PUBLISHER_TEMPLATE_EDITOR' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PUBLISHER_SECTION_EDITOR' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_TEMPLATE_LIBRARY_ACCESS' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PUBLISHER_TEMPLATE_VIEWER' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_DOCUMENT_AUTHOR' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_DOCUMENT_REVIEWER' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_DOCUMENT_APPROVER' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_BQA_EDITOR' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_RUN_PRIORITY_RPT' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PERIODIC_CONFIGURATION_CRUD' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_PERIODIC_CONFIGURATION_VIEW' and loc='ja'")
        sql("delete from localization where code='app.role.ROLE_SYSTEM_CONFIGURATION' and loc='ja'")

    }

    changeSet(author: "Vivek", id: "202419111356-01") {
        sql("update localization set text='提出済み' where code='app.icsrCaseState.SUBMITTED' and loc='ja'")
        sql("update localization set text='正常ACK' where code='app.icsrCaseState.COMMIT_ACCEPTED' and loc='ja'")
        sql("update localization set text='ACKエラー' where code='app.icsrCaseState.COMMIT_REJECTED' and loc='ja'")
        sql("update localization set text='スケジュール済み' where code='app.icsrCaseState.SCHEDULED' and loc='ja'")
        sql("update localization set text='提出不要' where code='app.icsrCaseState.SUBMISSION_NOT_REQUIRED' and loc='ja'")
        sql("update localization set text='提出不要' where code='app.icsrCaseState.SUBMISSION_NOT_REQUIRED_FINAL' and loc='ja'")
        sql("update localization set text='正常に症例番号{0}を提出済に変更しました。' where code='icsr.case.transmit.success' and loc='ja'")
        sql("update localization set text='症例が処理中のため、ICSRを作成できません' where code='icsr.case.add.localcp.case.state.active' and loc='ja'")
        sql("update localization set text='症例はまだ送信されておりません' where code='icsr.case.submit.not.transmitted.error' and loc='ja'")
    }

    changeSet(author: "shivam", id: "202411191456-01") {
        sql("update localization set text='複数成分を単一成分として扱う' where code='app.label.productDictionary.multi.ingredient' and loc='ja'")
        sql("update localization set text='複数成分を単一成分として扱う' where code='app.label.productDictionary.multi.substance' and loc='ja'")
        sql("update localization set text='注意' where code='app.warning.title' and loc='ja'")
        sql("update localization set text='WHO Drugを含める' where code='app.label.productDictionary.include.who.drugs' and loc='ja'")
        sql("update localization set text='Multi-Ingredient data cannot be added with Single Ingredient data' where code='app.warning.message' and loc='*'")
        sql("update localization set text='複数成分を単一成分として扱う場合、その他の単一成分と同時に使用することはできません' where code='app.warning.message' and loc='ja'")
        sql("update localization set text='Multi-Substance data cannot be added with Single Substance data' where code='app.warning.message.pvcm' and loc='*'")
        sql("update localization set text='複数成分を単一成分として扱う場合、その他の単一成分と同時に使用することはできません' where code='app.warning.message.pvcm' and loc='ja'")
        sql("update localization set text='Drug Record Numberが見つかりません' where code='app.productDictionary.fetchDrugRecord.error' and loc='ja'")
    }

    changeSet(author: "meenal", id: "20241119105-01") {
        sql("update localization set text='E2Bロケール' where code='app.label.template.xml.e2bLocale' and loc='ja'")
        sql("update localization set text='E2Bエレメント名ロケール' where code='app.label.template.xml.e2bElementNameLocale' and loc='ja'")
        sql("update localization set text='E2Bエレメント名' where code='app.label.template.xml.e2bElementName' and loc='ja'")
        sql("update localization set text='％を表示' where code='app.percentageOptionEnum.label' and loc='ja'")
        sql("update localization set text='条件付き書式' where code='app.dataTabulation.ConditionalFormatting' and loc='ja'")
        sql("update localization set text='追加Where句' where code='app.label.caseLineListing.advanced.custom.expression' and loc='ja'")
        sql("update localization set text='期間中の件数が1以上の場合のみ表示' where code='show.positiveCountOnly' and loc='ja'")
        sql("update localization set text='いいえ' where code='app.percentageOptionEnum.NO_PERCENTAGE' and loc='ja'")
        sql("update localization set text='合計' where code='app.percentageOptionEnum.BY_TOTAL' and loc='ja'")
        sql("update localization set text='小計' where code='app.percentageOptionEnum.BY_SUBTOTAL' and loc='ja'")
        sql("update localization set text='合計を非表示' where code='app.template.hideTotalRowCount' and loc='ja'")

    }

    changeSet(author: "Vivek", id: "202411201045-01") {
        sql("update localization set text='日時（ローカル時間）' where code='app.label.icsr.case.history.routed.date.preferred.timezone' and loc='ja'")
        sql("update localization set text='ローカル報告/Msg#' where code='app.label.localReportMsg' and loc='ja'")
    }

    changeSet(author: "Sahil", id: "202411201150-01") {
        sql("update localization set text='PV Reports - ICSRトラッキング管理' where code='app.icsr.case.tracking.title' and loc='ja'")
    }

    changeSet(author: "shivam", id: "202411201821-1") {
        sql("update localization set text='関連のアクションアイテム#' where code='app.actionItem.associatedActionItem.label' and loc='ja'")
    }

    changeSet(author: "meenal", id: "202411211624-01") {
        sql("update localization set text='PV Reports - 個別症例安全性報告プロファイル設定' where code='app.icsrProfileConf.title' and loc='ja'")
        sql("update localization set text='ICSRプロファイル設定' where code='app.icsrProfileConf.name.label' and loc='ja'")
    }

    changeSet(author: "shivam", id: "202411221608-1") {
        sql("update localization set text='組織名' where code='app.unitConfiguration.label.organizationName' and loc='ja'")
        sql("update localization set text='言語' where code='app.unitConfiguration.label.preferredLanguage' and loc='ja'")
        sql("update localization set text='添付ファイルルーティングID' where code='app.unitConfiguration.label.unitAttachmentRegId' and loc='ja'")
        sql("update localization set text='タイムゾーン' where code='app.unitConfiguration.label.timeZone' and loc='ja'")
        sql("update localization set text='MAH識別子' where code='app.unitConfiguration.label.holderId' and loc='ja'")
    }
  
    changeSet(author: "Vivek", id: "202411221459-01") {
        sql("update localization set text='理事長' where code='app.title.理事長'")
    }
  
    changeSet(author: "meenal", id: "202411221436-01") {
        sql("update localization set text='X分毎' where code='scheduler.minutely' and loc='ja'")
        sql("update localization set text='インバウンド Reason of Delay' where code='app.actionItemCategory.IN_DRILLDOWN_RECORD' and loc='ja'")
        sql("update localization set text='インバウンド Reason of Delay' where code='app.actionItemAppType.IN_DRILLDOWN_RECORD' and loc='ja'")
        sql("update localization set text='QBE レポート' where code='app.label.qbeForm' and loc='ja'")
        sql("update localization set text='時間毎' where code='scheduler.hours' and loc='ja'")
        sql("update localization set text='PV Reports - Eメールテンプレート作成' where code='app.emailTemplateConfiguration.create.title' and loc='ja'")
        sql("update localization set text='PV Reports - Eメールテンプレート編集' where code='app.emailTemplateConfiguration.edit.title' and loc='ja'")
        sql("update localization set text='PV Reports - Eメールテンプレートリスト' where code='app.emailTemplateConfiguration.list.title' and loc='ja'")
        sql("update localization set text='PV Reports - Eメールテンプレート閲覧' where code='app.emailTemplateConfiguration.show.title' and loc='ja'")
        sql("update localization set text='Eメール本文' where code='com.rxlogix.config.EmailTemplate.body.label' and loc='ja'")
    }

    changeSet(author: "meenal", id: "202411251207-01") {
        sql("update localization set text='ソース' where code='app.caseList.type.pvcm' and loc='ja'")
    }

    changeSet(author: "shivam", id: "202411261119-01") {
        sql("update localization set text='状態' where code='app.periodicReport.executed.workflowState.label' and loc='ja'")
    }

    changeSet(author: "shivam", id: "202411271410-01") {
        sql("update localization set text='更新' where code='app.update.button.label' and loc='ja'")
    }

    changeSet(author: "meenal", id: "202411261713-01") {
        sql("update localization set text='DLPからの対応期限日数' where code='app.label.dueInDaysPastDLP' and loc='ja'")
        sql("update localization set text='DLPからの対応期限日数は０以下には設定できません' where code='com.rxlogix.config.PeriodicReportConfiguration.dueInDays.min.notmet' and loc='ja'")
        sql("update localization set text='DLPからの対応期限日数は３６５日を超えて設定することはできません' where code='com.rxlogix.config.PeriodicReportConfiguration.dueInDays.max.exceeded' and loc='ja'")
        sql("update localization set text='アクションアイテムを作成する' where code='app.label.action.item.create.AI' and loc='ja'")
        sql("update localization set text='再審査申請' where code='app.periodicReportType.RESD' and loc='ja'")
    }
  
    changeSet(author: "meenal", id: "202411271225-01") {
        sql("update localization set text='ドキュメントプランニング' where code='app.label.topnav.report.request.plan' and loc='ja'")
        sql("update localization set text='ドキュメントカレンダー' where code='app.aggregate.report.calendar' and loc='ja'")
        sql("update localization set text='Excel形式でエクスポート' where code='app.ActionItem.export.label' and loc='ja'")
        sql("update localization set text='Excel形式でエクスポート' where code='app.balanceMinusQuery.export.label' and loc='ja'")
        sql("update localization set text='レポートを再作成しますか？' where code='icsr.regenerate.case.warning' and loc='ja'")
    }

    changeSet(author: "meenal", id: "202411281142-01") {
        sql("update localization set text='プライマリデータシートに基づき再評価する' where code='app.label.onPrimaryDatasheet' and loc='ja'")
        sql("update localization set text='特定日時点' where code='app.reassessListednessEnum.CUSTOM_START_DATE' and loc='ja'")
    }

    changeSet(author: "meenal", id: "202412021557-01") {
        sql("update localization set text='Case#{0} : Regenerating an ICSR Report for {1} profile.' where code='icsr.add.regenerate.case.success'")
    }

    changeSet(author: "ShubhamRx", id: "202411281207-02") {
        sql("update localization set text='Clinical Research and Measure Report' where code='app.label.icsr.profile.conf.multipleReporting' and loc='*'")
    }

    changeSet(author: "Vivek", id: "202412091240-01") {
        sql("update localization set text='作成エラー' where code='icsr.case.tracking.status.GENERATION_ERROR' and loc='ja'")
    }

    changeSet(author: "Siddharth", id: "20241210073545-7") {
        sql("update localization set text='報告期限' where code='icsr.profile.manual.schedule.dueInDays' and loc='ja' ")
    }

    changeSet(author: "Shivam", id: "202412181808-1") {
        sql("update localization set text='Case#{0} v{1}: Transmitted an ICSR Report for {2} profile' where code='icsr.case.transmit.success' ")
        sql("update localization set text='Case#{0} v{1}: Transmission error in the ICSR Report for {2} profile' where code='icsr.case.transmit.failed' ")
        sql("update localization set text='Case#{0} v{1}: Nullified an ICSR Report for {2} profile' where code='icsr.public.api.nullify.report.success' and loc='*' ")
    }
    changeSet(author: "Sahil", id: "202412201245-01") {
        sql("update localization set text='承認が必要' where code='app.label.workflow.rule.needApproval' and loc='ja'")
        sql("update localization set text='レポートの種類を選択してください' where code='reportType.noSelection.selectPrompt' and loc='ja'")
        sql("update localization set text='初回ステータスを選択してください' where code='initialState.noSelection.selectPrompt' and loc='ja'")
        sql("update localization set text='ターゲットステータスを選択してください' where code='targetState.noSelection.selectPrompt' and loc='ja'")
        sql("update localization set text='デフォルトアクションを選択を選択してください' where code='workflowRule.defaultAction.noSelection.selectPrompt' and loc='ja'")
        sql("update localization set text='ワークフローステータス' where code='app.label.workflow.State.index' and loc='ja'")
        sql("update localization set text='ワークフローステータスの新規作成' where code='app.label.workflow.state.create' and loc='ja'")
        sql("update localization set text='ワークフロールールの新規作成' where code='app.label.workflow.rule.create' and loc='ja'")
    }

    changeSet(author: "Sahil", id: "202501071715-01") {
        sql("update localization set text='ステータス' where code='app.periodicReport.executed.workflowState.label' and loc='ja'")
        sql("update localization set text='ステータス' where code='icsr.case.tracking.state' and loc='ja'")
    }

    changeSet(author: "meenal", id: "202501081647-01") {
        sql("update localization set text='ICSRレポートの情報を正常に更新しました。' where code='icsr.report.unsubmitting.status' and loc='ja'")
        sql("update localization set text='ICSRレポートの情報を更新中にエラーが発生しました。' where code='icsr.report.unsubmitting.status.error' and loc='ja'")
    }

    changeSet(author: "Sahil", id: "202501101815-01") {
        sql("update localization set text='F/U #' where code='app.label.followUpType' and loc='ja'")
        sql("update localization set text='F/Uタイプ' where code='app.label.followUpInfo' and loc='ja'")
        sql("update localization set text='症例シリーズライブラリ' where code='caseSeries.library.label' and loc='ja'")
        sql("update localization set text='XMLテンプレートの新規作成' where code='app.newTemplate.xml.menu' and loc='ja'")
        sql("update localization set text='期限超過' where code='app.widget.overdue' and loc='ja'")
        sql("update localization set text='期限間近' where code='app.widget.dueSoon' and loc='ja'")
        sql("update localization set text='対応中' where code='app.widget.inProgress' and loc='ja'")
        sql("update localization set text='オープン' where code='app.widget.opened' and loc='ja'")
        sql("update localization set text='定期報告サマリ' where code='app.widget.button.aggregate.label' and loc='ja'")
        sql("update localization set text='期限間近' where code='app.label.dueSoonCount' and loc='ja'")
        sql("update localization set text='直近に提出済' where code='app.label.submittedRecentlyCount' and loc='ja'")
        sql("update localization set text='スケジュール済' where code='app.label.scheduledCount' and loc='ja'")
    }
    changeSet(author: "Siddharth", id: "20250113060353-7") {
        sql("update localization set text='Positive Ack' where code='icsr.case.tracking.status.COMMIT_RECEIVED' and loc='*' ")
        sql("update localization set text='Message Error' where code='icsr.case.tracking.status.PARSER_REJECTED' and loc='*' ")
        sql("update localization set text='Negative Ack (Transmitted)' where code='icsr.case.tracking.status.TRANS_SUCCESS' and loc='*' ")
        sql("update localization set text='正常ACK' where code='icsr.case.tracking.status.COMMIT_RECEIVED' and loc='ja' ")
    }

    changeSet(author: "meenal", id: "202501161245-01") {
        sql("update localization set text='古いバージョンを削除する' where code='app.label.removeOldVersion' and loc='ja'")
        sql("update localization set text='ユーザーグループ' where code='app.label.userGroup' and loc='ja'")
        sql("update localization set text='ユーザーグループ' where code='userGroup.label' and loc='ja'")
        sql("update localization set text='ユーザーグループ' where code='user.group.label' and loc='ja'")
        sql("update localization set text='開始日' where code='app.label.reportRequest.reportingPeriodStart' and loc='ja'")
        sql("update localization set text='開始日' where code='app.Task.BaseDate.REPORT_PERIOD_START' and loc='ja'")
        sql("update localization set text='終了日' where code='app.label.reportRequest.reportingPeriodEnd' and loc='ja'")
        sql("update localization set text='終了日' where code='app.Task.BaseDate.REPORT_PERIOD_END' and loc='ja'")
        sql("update localization set text='データの時点' where code='app.Task.BaseDate.AS_OF_DATE' and loc='ja'")
        sql("update localization set text='レポート作成後' where code='app.label.deliveryOptions.task.onexecution' and loc='ja'")
        sql("update localization set text='日前（レポートの作成日）' where code='app.label.deliveryOptions.task.beforeExecution' and loc='ja'")
    }

    changeSet(author: "Gunjan", id: "202501121926") {
        sql("update localization set text='PVC Only show My or My Group RCA' where code='app.role.ROLE_USER_GROUP_RCA' and loc='*' ")
    }

    changeSet(author: "Shivam", id: "202501271439") {
        sql("update localization set text='yyyy/MM/dd hh:mm:ss z' where code='default.date.format' and loc='ja' ")
        sql("update localization set text='{0} {1}正常に削除されました。' where code='default.delete.message' and loc='ja' ")
        sql("update localization set text='レポートの設定する' where code='default.button.configureReport.label' and loc='ja' ")
        sql("update localization set text='共有先' where code='app.label.shared' and loc='ja' ")
        sql("update localization set text='添付ファイルのサイズが上限を超えています' where code='app.label.attachment.file.exceeded.max.size.message' and loc='ja' ")
        sql("update localization set text='パブリッシャーセクション' where code='app.configurationType.PUBLISHER_SECTION' and loc='ja' ")
        sql("update localization set text='パブリッシャーセクション' where code='app.workflowConfigurationType.PUBLISHER_SECTION' and loc='ja' ")
        sql("update localization set text='パブリッシャードキュメント' where code='app.workflowConfigurationType.PUBLISHER_FULL' and loc='ja' ")
        sql("update localization set text='パブリッシャードキュメントQC' where code='app.workflowConfigurationType.PUBLISHER_FULL_QC' and loc='ja' ")
        sql("update localization set text='ICSRレポート' where code='app.workflowConfigurationType.ICSR_REPORT' and loc='ja' ")
        sql("update localization set text='症例' where code='app.workflowConfigurationType.CASE' and loc='ja' ")
        sql("update localization set text='PV Central:インバウンドコンプライアンス' where code='app.workflowConfigurationType.PVC_INBOUND' and loc='ja' ")
        sql("update localization set text='少なくとも 1 つのレポート テンプレートが必要です' where code='com.rxlogix.config.IcsrReportConfiguration.templateQueries.minSize.notmet' and loc='ja' ")
        sql("update localization set text='少なくとも 1 つのレポート テンプレートが必要です' where code='com.rxlogix.config.IcsrProfileConfiguration.templateQueries.minSize.notmet' and loc='ja' ")
        sql("update localization set text='クエリ名にシングルクォーテーションまたはダブルクォーテーションは使用できません' where code='com.rxlogix.config.SuperQuery.name.validation' and loc='ja' ")
        sql("update localization set text='1 つの列セットに、同じカウント タイプのメジャーを重複させることはできません。' where code='com.rxlogix.config.DataTabulationTemplate.measures.duplicates' and loc='ja' ")
        sql("update localization set text='テーマを選択する必要があります' where code='com.rxlogix.user.Preference.theme.nullable' and loc='ja' ")
        sql("update localization set text='指定されたユーザー名のユーザーはすでに存在しています。ユーザーのldapプロパティを確認してください。' where code='com.rxlogix.user.User.name.unique.per.user' and loc='ja' ")
        sql("update localization set text='タイムゾーンを選択してください' where code='app.preference.timeZone.select' and loc='ja' ")
        sql("update localization set text='同一症例の提出' where code='app.preference.ROD.similar.cases' and loc='ja' ")
        sql("update localization set text='監査ログの詳細' where code='auditLog.label.details' and loc='ja' ")
        sql("update localization set text='インターバル/累積' where code='app.percentageOptionEnum.INTERVAL_TO_CUMULATIVE' and loc='ja' ")
        sql("update localization set text='Pre-Mart ETLステータス' where code='pre.mart.etl.execution.status' and loc='ja' ")
        sql("update localization set text='Affiliate ETLステータス' where code='affiliate.etl.execution.status' and loc='ja' ")
        sql("update localization set text='Mart ETLステータス' where code='etl.execution.status' and loc='ja' ")
        sql("update localization set text='ETLステータス' where code='app.etlStatus.label' and loc='ja' ")
        sql("update localization set text='未開始' where code='app.etlStatus.ETL_NOT_STARTED' and loc='ja' ")
        sql("update localization set text='すべてのETLの後' where code='scheduler.afterEveryEtl' and loc='ja' ")
        sql("update localization set text='全てのタイムフレーム' where code='show.allTimeframes' and loc='ja' ")
        sql("update localization set text='添付ファイルを削除しますか？' where code='issue.delete.attachment' and loc='ja' ")
        sql("update localization set text='本当に選択した添付ファイルを削除しますか？' where code='issue.delete.multi.attachment' and loc='ja' ")
        sql("update localization set text='ETLが現在実行中のため、JOBのスケジュールの変更ができません' where code='etl.running.update.request.failed' and loc='ja' ")
        sql("update localization set text='ETLが現在実行中のため、JOBを停止できません' where code='etl.running.disable.schedule.request.failed' and loc='ja' ")
        sql("update localization set text='正常にエクスポート完了しました{0}' where code='app.notification.export' and loc='ja' ")
        sql("update localization set text='エクスポート処理中にエラーが発生しました{0}' where code='app.notification.error' and loc='ja' ")
        sql("update localization set text='クエリの種類' where code='app.label.QueryType' and loc='ja' ")
        sql("update localization set text='レポートリンク' where code='app.label.action.item.report' and loc='ja' ")
        sql("update localization set text='症例リンク' where code='app.label.action.item.case.link' and loc='ja' ")
        sql("update localization set text='アクションオーナー' where code='app.label.action.item.owner' and loc='ja' ")
        sql("update localization set text='レポートの発行を承認するには、ログインパスワードを入力してください' where code='app.label.workflow.rule.needApproval.transmission' and loc='ja' ")
        sql("update localization set text='レポートの提出を承認するには、ログインパスワードを入力してください' where code='app.label.workflow.rule.needApproval.submission' and loc='ja' ")
        sql("update localization set text='Embaseをレポートのデータソース元として使用することはできません。' where code='embase.report.source.selection' and loc='ja' ")
        sql("update localization set text='承認日' where code='report.submission.approvalDate' and loc='ja' ")
        sql("update localization set text='症例サンプリングレコードがグループにアサインされました' where code='pvq.caseSampling.email.subject.group.label' and loc='ja' ")
        sql("update localization set text='イシューを作成' where code='app.label.quality.create.report.issue' and loc='ja' ")
        sql("update localization set text='バリーデーション処理をスキップするには、/* SKIP_VALIDATION */を追加してください' where code='app.query.customSQL.skipValidation' and loc='ja' ")
        sql("update localization set text='\"MS Officeでは一部の機能がサポートされておりません。MS Office形式でエクスポートする場合は\"\"図を画像としてエクスポート\"\"をアンチェックしてください\"' where code='app.data.tabulation.measures.imageAsChart.warningModal' and loc='ja' ")
        sql("update localization set text='{0} のインバウンドコンプライアンスの作成に成功しました。' where code='app.notification.inboundCompliance.generated' and loc='ja' ")
        sql("update localization set text='{0} のインバウンドコンプライアンスの作成に失敗しました。' where code='app.notification.inboundCompliance.failed' and loc='ja' ")
        sql("update localization set text='ドキュメント計画ページに表示' where code='app.label.reportRequestType.showInPlan' and loc='ja' ")
        sql("update localization set text='レポートリクエストがあるため、このパラメータの変更はできません。' where code='app.label.reportRequestType.aggWarn' and loc='ja' ")
        sql("update localization set text='Pre-Mart ETL最終正常完了日' where code='pre.mart.etl.lastRun.dateTime' and loc='ja' ")
        sql("update localization set text='Affiliate ETL最終正常完了日' where code='affiliate.etl.lastRun.dateTime' and loc='ja' ")
        sql("update localization set text='ETL 最終正常完了日' where code='etl.lastRun.dateTime' and loc='ja' ")
        sql("update localization set text='現在の日付以前に期限日を設定することはできません' where code='app.report.request.dueDate.before.now' and loc='ja' ")
        sql("update localization set text='完了日は、現在の日付より小さくすることはできません' where code='app.report.request.completionDate.before.now' and loc='ja' ")
        sql("update localization set text='遅延理' where code='app.actionItemCategory.DRILLDOWN_RECORD' and loc='ja' ")
        sql("update localization set text='アクションプラン' where code='app.actionItemAppType.ACTION_PLAN' and loc='ja' ")
        sql("update localization set text='-    Excelファイルの最初のセルは、症例番号のヘッダーです' where code='copy.paste.modal.values.first.cell.case.number' and loc='ja' ")
        sql("update localization set text='X分毎' where code='app.frequency.MINUTELY' and loc='ja' ")
        sql("update localization set text='E2Bエレメント' where code='app.label.template.xml.e2b' and loc='ja' ")
        sql("update localization set text='ソースフィールドラベル' where code='app.label.template.xml.sourceFieldLabel' and loc='ja' ")
        sql("update localization set text='PV 品質 - イシューの作成' where code='app.quality.title.quality.issue.create' and loc='ja' ")
        sql("update localization set text='イシューを表示' where code='app.quality.title.issue.view' and loc='ja' ")
        sql("update localization set text='添付' where code='quality.attachment.label' and loc='ja' ")
        sql("update localization set text='追加したユーザー' where code='quality.added.by.label' and loc='ja' ")
        sql("update localization set text='ブラウズ' where code='quality.browse.label' and loc='ja' ")
        sql("update localization set text='添付' where code='quality.attach.label' and loc='ja' ")
        sql("update localization set text='追加日' where code='quality.date.added.label' and loc='ja' ")
        sql("update localization set text='アクション' where code='quality.actions.label' and loc='ja' ")
        sql("update localization set text='添付ファイルは正常に削除されました。' where code='quality.attachment.removed' and loc='ja' ")
        sql("update localization set text='複数の添付ファイルは許可されていません。一度に1つの添付ファイルを選択してください。' where code='quality.multi.select.attachment' and loc='ja' ")
        sql("update localization set text='イシュー番号は必須です' where code='com.rxlogix.config.Capa8D.issueNumber.nullable' and loc='ja' ")
        sql("update localization set text='イシュー番号は一意である必要があります' where code='com.rxlogix.config.Capa8D.issueNumber.unique' and loc='ja' ")
        sql("update localization set text='イシュー番号は200文字を超えて設定することはできません' where code='com.rxlogix.config.Capa8D.issueNumber.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='イシュータイプは255文字を超えて設定することはできません' where code='com.rxlogix.config.Capa8D.issueType.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='カテゴリーは255文字を超えて設定することはできません' where code='com.rxlogix.config.Capa8D.category.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='コメント欄に2000文字以上、入力することはできません' where code='com.rxlogix.config.Capa8D.comments.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='根本的な原因欄に2000文字以上、入力することはできません' where code='com.rxlogix.config.Capa8D.rootCause.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='確認結果欄に2000文字以上、入力することはできません' where code='com.rxlogix.config.Capa8D.verificationResults.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='有効なイシュー番号を入力してください' where code='com.rxlogix.config.Capa8D.issueNumber.typeMismatch.error' and loc='ja' ")
        sql("update localization set text='備考欄に2000文字以上、入力することはできません' where code='com.rxlogix.config.Capa8D.remarks.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='PVCメイン' where code='app.dashboard.DashboardEnum.PVC_MAIN' and loc='ja' ")
        sql("update localization set text='レポートタスクテンプレート' where code='app.TaskTemplateTypeEnum.AGGREGATE_REPORTS' and loc='ja' ")
        sql("update localization set text='パブリッシャーセクションテンプレート' where code='app.TaskTemplateTypeEnum.PUBLISHER_SECTION' and loc='ja' ")
        sql("update localization set text='投稿者' where code='com.rxlogix.config.TaskTemplate.assignToContributor' and loc='ja' ")
        sql("update localization set text='著者' where code='com.rxlogix.config.TaskTemplate.assignToAuthor' and loc='ja' ")
        sql("update localization set text='承認' where code='com.rxlogix.config.TaskTemplate.assignToApprover' and loc='ja' ")
        sql("update localization set text='レビューワー' where code='com.rxlogix.config.TaskTemplate.assignToReviewer' and loc='ja' ")
        sql("update localization set text='データ分析ファイルの作成' where code='app.spotfire.caseSeries.generate.spotfire' and loc='ja' ")
        sql("update localization set text='この名称はすでに使用されていますので、別の名称を使用してください。' where code='com.rxlogix.config.EmailTemplate.name.unique' and loc='ja' ")
        sql("update localization set text='遅延理' where code='app.reasonOfDelay.title' and loc='ja' ")
        sql("update localization set text='版数' where code='app.pvc.version' and loc='ja' ")
        sql("update localization set text='遅延' where code='rod.lateType.LATE_INB' and loc='ja' ")
        sql("update localization set text='遅延では泣き' where code='rod.lateType.NOT_LATE_INB' and loc='ja' ")
        sql("update localization set text='イシュータイプ' where code='rod.fieldType.Issue_Type' and loc='ja' ")
        sql("update localization set text='根本原因' where code='rod.fieldType.Root_Cause' and loc='ja' ")
        sql("update localization set text='根本原因分類' where code='rod.fieldType.Root_Cause_Class' and loc='ja' ")
        sql("update localization set text='根本原因サブカテゴリ―' where code='rod.fieldType.Root_Cause_Sub_Cat' and loc='ja' ")
        sql("update localization set text='責任者' where code='rod.fieldType.Resp_Party' and loc='ja' ")
        sql("update localization set text='是正措置' where code='rod.fieldType.Corrective_Action' and loc='ja' ")
        sql("update localization set text='予防措置' where code='rod.fieldType.Preventive_Action' and loc='ja' ")
        sql("update localization set text='是正措置実施日' where code='rod.fieldType.Corrective_Date' and loc='ja' ")
        sql("update localization set text='予防措置実施日' where code='rod.fieldType.Preventive_Date' and loc='ja' ")
        sql("update localization set text='調査' where code='rod.fieldType.Investigation' and loc='ja' ")
        sql("update localization set text='サマリー' where code='rod.fieldType.Summary' and loc='ja' ")
        sql("update localization set text='アクション' where code='rod.fieldType.Actions' and loc='ja' ")
        sql("update localization set text='すべて表示' where code='app.RCA.display.list' and loc='ja' ")
        sql("update localization set text='表示' where code='app.RCA.hidden' and loc='ja' ")
        sql("update localization set text='編集可能者' where code='label.rcaMapping.editableBy' and loc='ja' ")
        sql("update localization set text='PV Central (アウトバウンド)' where code='label.rcaMapping.app.PVCentral' and loc='ja' ")
        sql("update localization set text='PV Central(インバウンド)' where code='label.rcaMapping.app.Inbound' and loc='ja' ")
        sql("update localization set text='対応待ちアクション' where code='app.icsrCaseState.PENDING_ACTIONS' and loc='ja' ")
        sql("update localization set text='期限超過及び期限間近' where code='app.icsrCaseState.OVERDUE_AND_DUE_SOON' and loc='ja' ")
        sql("update localization set text='Case#{0} v{1}: {2} profileのICSRレポートを送信しました。' where code='icsr.case.transmit.success' and loc='ja' ")
        sql("update localization set text='Case#{0} v{1}: {2} profileのICSRレポートを送信中にエラーが発生しました。' where code='icsr.case.transmit.failed' and loc='ja' ")
        sql("update localization set text='ドリルダウンパラメータ' where code='app.label.drillDownParameters' and loc='ja' ")
        sql("update localization set text='観察 -> 優先度 -> エラー タイプ' where code='app.actionPlan.groupping.observation_priority_issue' and loc='ja' ")
        sql("update localization set text='優先度 -> エラー タイプ' where code='app.actionPlan.groupping.priority_issue' and loc='ja' ")
        sql("update localization set text='担当部門 -> 優先度' where code='app.actionPlan.groupping.responsible_priority' and loc='ja' ")
        sql("update localization set text='アクションプランのサマリーテキストを空欄にすることはできません' where code='app.actionPlan.summaryText.blank' and loc='ja' ")
        sql("update localization set text='PV Central - ダッシュボード' where code='app.central.dashboard.title' and loc='ja' ")
        sql("update localization set text='レポート' where code='app.executionStatus.configType.REPORTS' and loc='ja' ")
        sql("update localization set text='ICSRプロファイル' where code='app.executionStatus.configType.ICSR_PROFILE' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンス' where code='app.executionStatus.configType.INBOUND_COMPLIANCE' and loc='ja' ")
        sql("update localization set text='DLP日起算からの期限（日）' where code='app.label.reportRequest.dueDateToHa' and loc='ja' ")
        sql("update localization set text='DLP日起算からの期限' where code='app.label.reportRequest.dueDateToHa2' and loc='ja' ")
        sql("update localization set text='幅(最小:1、最大:12)' where code='app.label.reportRequestField.width' and loc='ja' ")
        sql("update localization set text='セクション' where code='app.label.reportRequestField.section' and loc='ja' ")
        sql("update localization set text='症例#{0}:プロファイル{1}のICSRレポートスケジュールしました。' where code='icsr.add.manual.case.success' and loc='ja' ")
        sql("update localization set text='症例#{0} : プロファイル{1}の ICSR レポートの再作成中' where code='icsr.add.regenerate.case.success' and loc='ja' ")
        sql("update localization set text='アップグレード前に作成されたレポートは再再作成できません。' where code='icsr.add.regenerate.case.fail' and loc='ja' ")
        sql("update localization set text='一括再作成は、{0}ケースで成功し、{1}ケースで失敗しました。' where code='icsr.case.bulk.regenerate.message' and loc='ja' ")
        sql("update localization set text='この症例の、ICSRはすでに作成中です。レポートが作成されるまでしばらくお待ちください。' where code='icsr.generate.manual.qualified.for.generation' and loc='ja' ")
        sql("update localization set text='症例#{0} の ICSR プレビューの作成中。実行状況を確認するためにページを更新してください。' where code='icsr.generate.manual.request.success' and loc='ja' ")
        sql("update localization set text='現在、他の症例のICSRの処理が行われています。他の症例については、しばらくしてから再度実行してください。' where code='icsr.generate.manual.no.slot' and loc='ja' ")
        sql("update localization set text='スケジュールされたプロファイルに対して症例#{0}を再評価しています。' where code='icsr.reEvaluate.manual.case.success' and loc='ja' ")
        sql("update localization set text='作成された {0} の ICSR プレビュー' where code='app.notification.icsr.case.number.preview.ready' and loc='ja' ")
        sql("update localization set text='{0} の ICSR プレビューを作成できませんでした' where code='app.notification.icsr.case.number.preview.failed' and loc='ja' ")
        sql("update localization set text='ダイナミックドリルダウン機能はHTML形式ではサポートされていません。この機能を使用するには、必要なインタラクティブセクション出力を選択してください' where code='app.dynamic.drill.interactiveNotSupported' and loc='ja' ")
        sql("update localization set text='E2B R2 Report Form は XML 形式でのみサポートされているため、確認にはレポートをダウンロードしてごご確認ください。' where code='app.e2b.r2.icsr.support.only.xml.format' and loc='ja' ")
        sql("update localization set text='EC-MIRレポートフォームはXML形式でのみ対応しているため、確認にはレポートをダウンロードしてご確認ください。' where code='app.mir.r2.icsr.support.only.xml.format' and loc='ja' ")
        sql("update localization set text='ユーザーと共有' where code='app.label.generatedReports.sharedWith' and loc='ja' ")
        sql("update localization set text='グループと共有' where code='app.label.generatedReports.sharedWithGroup' and loc='ja' ")
        sql("update localization set text='バージョン' where code='app.label.generatedReports.version' and loc='ja' ")
        sql("update localization set text='実行された構成' where code='app.label.generatedReports.executedConfiguration' and loc='ja' ")
        sql("update localization set text='実行済症例シリーズ' where code='app.label.generatedReports.executedCaseSeries' and loc='ja' ")
        sql("update localization set text='トークンの生成' where code='mail.auth.generate.token.btn.label' and loc='ja' ")
        sql("update localization set text='テストメール' where code='mail.auth.test.email.btn.label' and loc='ja' ")
        sql("update localization set text='E メール OAuth トークン' where code='mail.auth.configuration.title.label' and loc='ja' ")
        sql("update localization set text='パブリッシャー　コントリビュータ' where code='app.publisher.publisherContributors' and loc='ja' ")
        sql("update localization set text='パブリッシャー　コントリビュータ' where code='app.publisher.publisherContributor' and loc='ja' ")
        sql("update localization set text='エラー：メールの設定に問題があります' where code='app.report.mail.configuration.exception' and loc='ja' ")
        sql("update localization set text='エラー：このファイルのJasperの最大タイムアウト制限を超えました' where code='app.report.mail.timeout.exception' and loc='ja' ")
        sql("update localization set text='エラー：このファイルの 列数がjasper の出力ページ サイズの制限を超えています。' where code='app.report.mail.jre.exception' and loc='ja' ")
        sql("update localization set text='エラー：予期しないエラーが発生しました' where code='app.report.mail.default' and loc='ja' ")
        sql("update localization set text='エラー：認証に問題があります。' where code='app.report.mail.auth.exception' and loc='ja' ")
        sql("update localization set text='PV Central - イシューレポートの作成' where code='app.central.title.central.issue.create' and loc='ja' ")
        sql("update localization set text='PV Central - イシュー報告一覧' where code='app.central.title.central.issue.list' and loc='ja' ")
        sql("update localization set text='PV Central - イシューレポートの表示' where code='app.central.title.central.issue.view' and loc='ja' ")
        sql("update localization set text='PV Central - イシューレポートの編集' where code='app.central.title.central.issue.edit' and loc='ja' ")
        sql("update localization set text='このプロファイルでは、手動と自動スケジュールを同時に使用することはできません' where code='com.rxlogix.config.configuration.auto.manual.both.cannot.be.true' and loc='ja' ")
        sql("update localization set text='有効な設定を更新します。理由を入力してください。' where code='app.report.justification' and loc='ja' ")
        sql("update localization set text='集計レポートのスケジュールを解除します。理由を追加してください。' where code='app.unschedule.justification' and loc='ja' ")
        sql("update localization set text='すべての更新の理由を入力してください。' where code='app.bulk.justification' and loc='ja' ")
        sql("update localization set text='すべての更新の理由を入力して。' where code='app.add.justification' and loc='ja' ")
        sql("update localization set text='PV Publisher - ドキュメントカレンダーカレンダー' where code='app.aggregate.report.calendar.title' and loc='ja' ")
        sql("update localization set text='次回のスケジュール済みレポートのみを表示' where code='app.label.nextOnlyFilter' and loc='ja' ")
        sql("update localization set text='レポートリクエストの表示' where code='app.label.showReportRequestFilter' and loc='ja' ")
        sql("update localization set text='レポートを表示' where code='app.label.showReportFilter' and loc='ja' ")
        sql("update localization set text='バージョン名' where code='app.label.versionName' and loc='ja' ")
        sql("update localization set text='バージョン名パターン' where code='app.label.versionNamePattern' and loc='ja' ")
        sql("update localization set text='次のタグをパターンで使用できます' where code='app.label.generatedReportPatternHelp.text1' and loc='ja' ")
        sql("update localization set text='書類の提出' where code='app.submitting.document' and loc='ja' ")
        sql("update localization set text='形式' where code='app.submitting.format' and loc='ja' ")
        sql("update localization set text='基本情報' where code='app.ReportRequestField.section.BASE' and loc='ja' ")
        sql("update localization set text='スケジュール情報' where code='app.ReportRequestField.section.SCHEDULE' and loc='ja' ")
        sql("update localization set text='追加情報' where code='app.ReportRequestField.section.ADDITIONAL' and loc='ja' ")
        sql("update localization set text='コメント' where code='app.ReportRequestField.section.COMMENTS' and loc='ja' ")
        sql("update localization set text='無効化' where code='app.label.reportRequestField.disabled' and loc='ja' ")
        sql("update localization set text='注:このフィールドを空白のままにするか、有効なJavaScriptを入力してください' where code='app.reportRequestField.jscript.note' and loc='ja' ")
        sql("update localization set text='DLP日起算からのレポート期限' where code='app.Task.BaseDate.DUE_DATE' and loc='ja' ")
        sql("update localization set text='セクションの期限' where code='app.Task.BaseDate.SECTION_DUE_DATE' and loc='ja' ")
        sql("update localization set text='DLP日起算からのレポート期限（日）は定期帳票のみに有効な設定です。アドホックレポートに使用時には自動的に作成日と共にDLP日起算からの期限（日）に置き換えられます' where code='app.Task.BaseDate.warn' and loc='ja' ")
        sql("update localization set text='QCステータス' where code='app.label.qcstate' and loc='ja' ")
        sql("update localization set text='パースエラーにより値を取得できません' where code='app.pvp.composer.warning' and loc='ja' ")
        sql("update localization set text='値の取得元' where code='app.pvp.composer.fetchFrom' and loc='ja' ")
        sql("update localization set text='現在のレポート' where code='app.pvp.composer.currentReport' and loc='ja' ")
        sql("update localization set text='前回のレポート' where code='app.pvp.composer.previousReport' and loc='ja' ")
        sql("update localization set text='開始' where code='app.pvp.composer.from' and loc='ja' ")
        sql("update localization set text='セクション' where code='app.pvp.composer.section' and loc='ja' ")
        sql("update localization set text='ソース' where code='app.pvp.composer.source' and loc='ja' ")
        sql("update localization set text='公開されたドキュメント' where code='app.pvp.composer.doc' and loc='ja' ")
        sql("update localization set text='名前付き' where code='app.pvp.composer.withName' and loc='ja' ")
        sql("update localization set text='付き' where code='app.pvp.composer.with' and loc='ja' ")
        sql("update localization set text='テーブル (レポート出力結果からテーブルを取得)' where code='app.pvp.composer.table' and loc='ja' ")
        sql("update localization set text='チャート(データ集計レポートの出力結果からチャートを取得)' where code='app.pvp.composer.chart' and loc='ja' ")
        sql("update localization set text='セル (セクションの出力結果からセル値を取得)' where code='app.pvp.composer.cell' and loc='ja' ")
        sql("update localization set text='範囲(セクションの出力結果からセル範囲を取得)' where code='app.pvp.composer.range' and loc='ja' ")
        sql("update localization set text='データ (セクションの出力結果からデータを取得する)' where code='app.pvp.composer.data' and loc='ja' ")
        sql("update localization set text='コンテンツ(Excel、PDF、Word、IMGからコンテンツ全体を取得)' where code='app.pvp.composer.content' and loc='ja' ")
        sql("update localization set text='テキスト (Wordからテキストを取得)' where code='app.pvp.composer.text' and loc='ja' ")
        sql("update localization set text='段落(Wordから書式設定されたテキストを取得)' where code='app.pvp.composer.paragraph' and loc='ja' ")
        sql("update localization set text='ブックマーク(Wordからブックマーク間のテキストを取得)' where code='app.pvp.composer.bookmark' and loc='ja' ")
        sql("update localization set text='セル(Excelからセル値を取得)' where code='app.pvp.composer.cell2' and loc='ja' ")
        sql("update localization set text='範囲(Excelからセル範囲を取得)' where code='app.pvp.composer.range2' and loc='ja' ")
        sql("update localization set text='Img (PDFページを画像として取得)' where code='app.pvp.composer.img' and loc='ja' ")
        sql("update localization set text='データ(excel,json,xmlからデータを取得)' where code='app.pvp.composer.data2' and loc='ja' ")
        sql("update localization set text='テキスト (公開されたドキュメントからテキストを取得)' where code='app.pvp.composer.text2' and loc='ja' ")
        sql("update localization set text='段落(公開されたドキュメントから書式設定されたテキストを取得)' where code='app.pvp.composer.paragraph2' and loc='ja' ")
        sql("update localization set text='ブックマーク(公開されたドキュメントからブックマーク間のテキストを取得)' where code='app.pvp.composer.bookmark2' and loc='ja' ")
        sql("update localization set text='行番号:' where code='app.pvp.composer.rowNum' and loc='ja' ")
        sql("update localization set text='最後の列または行番号には「last」キーワードを使用できます。例：、「last-1」は最後から１つ前の番号を意味します' where code='app.pvp.composer.cellInfo' and loc='ja' ")
        sql("update localization set text='開始行番号:' where code='app.pvp.composer.fromRowNum' and loc='ja' ")
        sql("update localization set text='開始列番号:' where code='app.pvp.composer.fromColNum' and loc='ja' ")
        sql("update localization set text='終了行番号:' where code='app.pvp.composer.toRowNum' and loc='ja' ")
        sql("update localization set text='終了行番号:' where code='app.pvp.composer.toColNum' and loc='ja' ")
        sql("update localization set text='最後の列または行番号には「last」キーワードを使用できます。例：、「last-1」は最後から１つ前の番号を意味します' where code='app.pvp.composer.rangeInfo' and loc='ja' ")
        sql("update localization set text='開始：' where code='app.pvp.composer.startsFrom' and loc='ja' ")
        sql("update localization set text='終了：' where code='app.pvp.composer.endsTo' and loc='ja' ")
        sql("update localization set text='開始ページ：' where code='app.pvp.composer.fromPage' and loc='ja' ")
        sql("update localization set text='終了ページ：' where code='app.pvp.composer.toPage' and loc='ja' ")
        sql("update localization set text='空白のままにすることで、すべてのページが取得されます' where code='app.pvp.composer.imgInfo' and loc='ja' ")
        sql("update localization set text='結果：' where code='app.pvp.composer.result' and loc='ja' ")
        sql("update localization set text='ステート' where code='app.comparison.state' and loc='ja' ")
        sql("update localization set text='作成日' where code='app.comparison.created' and loc='ja' ")
        sql("update localization set text='比較日：' where code='app.comparison.compared' and loc='ja' ")
        sql("update localization set text='メッセージ' where code='app.comparison.message' and loc='ja' ")
        sql("update localization set text='比較結果' where code='app.comparison.results' and loc='ja' ")
        sql("update localization set text='レポートの比較は正常に行われました。下記のテーブルをご確認ください' where code='app.comparison.success' and loc='ja' ")
        sql("update localization set text='実行中にエラーが発生しました。次のログを参照してください。' where code='app.comparison.params.error4' and loc='ja' ")
        sql("update localization set text='開始日は 終了日 より過去日である必要があります。' where code='app.comparison.params.error6' and loc='ja' ")
        sql("update localization set text='構成が正常に作成されました。比較はレポート実行後に行われます。' where code='app.comparison.params.success' and loc='ja' ")
        sql("update localization set text='レポートの比較中にエラーが発生しました。' where code='app.comparison.copy.comparison.error' and loc='ja' ")
        sql("update localization set text='比較の結果:' where code='app.comparison.resultOfComporison' and loc='ja' ")
        sql("update localization set text='比較の結果、両レポートに差異はありません。' where code='app.comparison.equal' and loc='ja' ")
        sql("update localization set text='選択したセクションの比較テンプレートタイプはサポートされていません。' where code='app.comparison.notSupported' and loc='ja' ")
        sql("update localization set text='レポートからの一意の行' where code='app.comparison.uniqueRows' and loc='ja' ")
        sql("update localization set text='レポート ID (コンマ区切り)' where code='app.comparison.reportsId' and loc='ja' ")
        sql("update localization set text='名前プレフィックス' where code='app.comparison.prefix' and loc='ja' ")
        sql("update localization set text='症例シリーズID(アドホックレポート用)' where code='app.comparison.caseSeries' and loc='ja' ")
        sql("update localization set text='レポート結果 ID' where code='app.comparison.reportResultId' and loc='ja' ")
        sql("update localization set text='ID{0}のレポートが見つかりませんでした。' where code='app.comparison.reportNotFound' and loc='ja' ")
        sql("update localization set text='集計レポートの比較にはドラフトまたは最終レポートが生成されている必要があります。' where code='app.comparison.log.agg' and loc='ja' ")
        sql("update localization set text='レポートのセクション数が異なるため、比較することができません。' where code='app.comparison.log.diffnumber' and loc='ja' ")
        sql("update localization set text='結果: 差異があります。セクションには別のテンプレートが存在します!<br>' where code='app.comparison.log.difftemplate' and loc='ja' ")
        sql("update localization set text='セクションの比較 \"' where code='app.comparison.log.sect' and loc='ja' ")
        sql("update localization set text='予期しないエラーが発生しました:' where code='app.comparison.log.err' and loc='ja' ")
        sql("update localization set text='セクションの比較中' where code='app.comparison.log.compsect' and loc='ja' ")
        sql("update localization set text='比較中' where code='app.comparison.log.comp' and loc='ja' ")
        sql("update localization set text='症例のXML比較' where code='app.comparison.log.compOfXml' and loc='ja' ")
        sql("update localization set text='症例' where code='app.comparison.log.case' and loc='ja' ")
        sql("update localization set text='検出された差異:' where code='app.comparison.log.differences' and loc='ja' ")
        sql("update localization set text='XMLに差異はありません' where code='app.comparison.log.XMLequal' and loc='ja' ")
        sql("update localization set text='セクション比較の結果、差異が存在します' where code='app.comparison.sectionsNotEquals' and loc='ja' ")
        sql("update localization set text='セクションの比較の結果、差異はありません' where code='app.comparison.sectionsEquals' and loc='ja' ")
        sql("update localization set text='PV Publisher - レポートの作成' where code='app.PvpCreateReport.label' and loc='ja' ")
        sql("update localization set text='PV Publisher - 集計レポート設定ライブラリ' where code='app.PvpPeriodicReportLibrary.title' and loc='ja' ")
        sql("update localization set text='PV Publisher - 送信履歴' where code='app.PvpsubmissionHistory.title' and loc='ja' ")
        sql("update localization set text='PV Publisher - レポートリクエスト一覧' where code='app.task.PvpreportRequestList.title' and loc='ja' ")
        sql("update localization set text='PV Publisher - プランテンプレート' where code='app.Pvpgantt.list.title' and loc='ja' ")
        sql("update localization set text='PVパブリッシャー - 区分' where code='app.pvp.viewCriteria.title' and loc='ja' ")
        sql("update localization set text='名称 (スペースや特殊文字は使用できません)' where code='app.reportRequest.customField.name' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンス' where code='app.label.pvc.inbound.compliance' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンスの概要' where code='app.label.pvc.inbound.compliance.overview' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンス設定' where code='app.label.pvc.inbound.compliance.configuration' and loc='ja' ")
        sql("update localization set text='設定ライブラリ' where code='app.label.pvc.inbound.configuration.library' and loc='ja' ")
        sql("update localization set text='パラメータ値を指定する必要があります' where code='com.rxlogix.config.QueryCompliance.parameterValues.valueless' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンスの作成' where code='app.label.inbound.compliance.create.title' and loc='ja' ")
        sql("update localization set text='許容されたタイムフレーム' where code='app.label.allow.timeframe' and loc='ja' ")
        sql("update localization set text='週末を含める' where code='app.label.include.weekends' and loc='ja' ")
        sql("update localization set text='休日を含める' where code='app.label.include.holidays' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンス設定が正常に作成されました' where code='inbound.compliance.created.message' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンス設定が正常に更新されました' where code='inbound.compliance.updated.message' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンス' where code='app.inboundCompliance.title' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンス設定' where code='app.inbound.compliance.configuration.label' and loc='ja' ")
        sql("update localization set text='送信者名' where code='app.label.sender.name' and loc='ja' ")
        sql("update localization set text='区分名' where code='app.label.criteria.name' and loc='ja' ")
        sql("update localization set text='クエリを選択する必要があります' where code='com.rxlogix.config.QueryCompliance.query.nullable' and loc='ja' ")
        sql("update localization set text='区分名は必須です' where code='com.rxlogix.config.QueryCompliance.criteriaName.nullable' and loc='ja' ")
        sql("update localization set text='区分名は 255 文字を超えることはできません' where code='com.rxlogix.config.QueryCompliance.criteriaName.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='区分名とクエリは必須です' where code='com.rxlogix.config.InboundCompliance.queriesCompliance.minSize.notmet' and loc='ja' ")
        sql("update localization set text='送信者名はすでに使用されています。別の名前を使用してください' where code='com.rxlogix.config.InboundCompliance.senderName.unique' and loc='ja' ")
        sql("update localization set text='送信者名を選択する必要があります' where code='com.rxlogix.config.InboundCompliance.senderName.nullable' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンスの表示' where code='app.label.inbound.compliance.view.title' and loc='ja' ")
        sql("update localization set text='実行されたインバウンドコンプライアンスの表示' where code='app.label.inbound.compliance.view.executed.title' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンスの編集' where code='app.label.inbound.compliance.edit.title' and loc='ja' ")
        sql("update localization set text='送信者の詳細' where code='app.label.sender.details' and loc='ja' ")
        sql("update localization set text='送信者識別情報を正常に保存しました' where code='app.sender.initialize.successful.msg' and loc='ja' ")
        sql("update localization set text='送信者の識別' where code='app.label.select.field' and loc='ja' ")
        sql("update localization set text='PV Central - インバウンドコンプライアンス設定ライブラリ' where code='app.pvcentral.inbound.reportLibrary.title' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンス設定ライブラリ' where code='app.pvcentral.inbound.reportLibrary.label' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンスの作成' where code='app.pvcentral.inbound.createCompliance.label' and loc='ja' ")
        sql("update localization set text='PV Central - インバウンドコンプライアンス' where code='app.pvcentral.inbound.compliance.title' and loc='ja' ")
        sql("update localization set text='インバウンドコンプライアンス' where code='app.pvcentral.inbound.compliance.label' and loc='ja' ")
        sql("update localization set text='PV Central - インバウンドコンプライアンス設定を表示' where code='app.pvcentral.inbound.viewCompliance.title' and loc='ja' ")
        sql("update localization set text='PV Central - 症例確認' where code='app.pvcentral.inbound.viewCases.title' and loc='ja' ")
        sql("update localization set text='症例を確認' where code='app.pvcentral.inbound.viewCases.label' and loc='ja' ")
        sql("update localization set text='送信者の受領日' where code='app.label.sender.receipt.date' and loc='ja' ")
        sql("update localization set text='ソーステーブル' where code='app.src.table.label' and loc='ja' ")
        sql("update localization set text='ターゲットテーブル' where code='app.tgt.table.label' and loc='ja' ")
        sql("update localization set text='ソース列名' where code='app.src.column.name.label' and loc='ja' ")
        sql("update localization set text='ターゲット列名' where code='app.tgt.column.name.label' and loc='ja' ")
        sql("update localization set text='ソース件数' where code='app.src.count.label' and loc='ja' ")
        sql("update localization set text='ターゲット件数' where code='app.tgt.count.label' and loc='ja' ")
        sql("update localization set text='経過時間 (分)' where code='app.elapsed.minutes.label' and loc='ja' ")
        sql("update localization set text='ソース値' where code='app.src.value.label' and loc='ja' ")
        sql("update localization set text='ターゲット値' where code='app.tgt.value.label' and loc='ja' ")
        sql("update localization set text='症例ID' where code='app.case.id.label' and loc='ja' ")
        sql("update localization set text='最終更新日時' where code='app.last.update.time.label' and loc='ja' ")
        sql("update localization set text='ヘルプメッセージ' where code='app.label.localizationHelp.appName' and loc='ja' ")
        sql("update localization set text='PV Report - ヘルプメッセージ一覧' where code='app.label.localizationHelp.list.title' and loc='ja' ")
        sql("update localization set text='ヘルプメッセージ一覧' where code='app.label.localizationHelp.list' and loc='ja' ")
        sql("update localization set text='コード' where code='app.label.localizationHelp.code' and loc='ja' ")
        sql("update localization set text='テキスト' where code='app.label.localizationHelp.text' and loc='ja' ")
        sql("update localization set text='ロケール' where code='app.label.localizationHelp.locale' and loc='ja' ")
        sql("update localization set text='PV Report - ヘルプメッセージの作成' where code='app.label.localizationHelp.create.title' and loc='ja' ")
        sql("update localization set text='ヘルプメッセージの作成' where code='app.label.localizationHelp.create' and loc='ja' ")
        sql("update localization set text='PV Report - ヘルプメッセージの編集' where code='app.label.localizationHelp.edit.title' and loc='ja' ")
        sql("update localization set text='ヘルプメッセージの編集' where code='app.label.localizationHelp.edit' and loc='ja' ")
        sql("update localization set text='ラベル' where code='app.label.localizationHelp.label' and loc='ja' ")
        sql("update localization set text='ヘルプメッセージの内容' where code='app.label.localizationHelp.helpContent' and loc='ja' ")
        sql("update localization set text='ラベル{0}のヘルプメッセージを正常に削除しました。' where code='app.label.localizationHelp.delete' and loc='ja' ")
        sql("update localization set text='ヘルプ' where code='app.label.localizationHelp.help' and loc='ja' ")
        sql("update localization set text='ヘルプ コンテンツの設定' where code='app.label.localizationHelp.menu' and loc='ja' ")
        sql("update localization set text='Sharepointのサイトとフォルダを選択してください' where code='app.label.localizationHelp.selectFolder' and loc='ja' ")
        sql("update localization set text='Sharepointでエラーが発生しました:' where code='app.label.localizationHelp.errorSharepoint' and loc='ja' ")
        sql("update localization set text='選択したSharepointフォルダにファイルが見つかりませんでした。' where code='app.label.localizationHelp.noFilesSharepoint' and loc='ja' ")
        sql("update localization set text='{0}ファイルパスが正常に更新されました。' where code='app.label.localizationHelp.filesUodateSuccessfully' and loc='ja' ")
        sql("update localization set text='ヘルプメッセージ' where code='app.label.localizationHelp.helpMessages' and loc='ja' ")
        sql("update localization set text='リリースノート' where code='app.label.localizationHelp.releaseNotes' and loc='ja' ")
        sql("update localization set text='リリースノート' where code='app.label.localizationHelp.releaseNote' and loc='ja' ")
        sql("update localization set text='リリースノート項目' where code='app.label.localizationHelp.releaseNoteItem' and loc='ja' ")
        sql("update localization set text='エクスポートするデータがありません。' where code='app.label.localizationHelp.nothingToExport' and loc='ja' ")
        sql("update localization set text='パースエラーによりインポートに失敗しました。正しいjsonファイルを選択してください。' where code='app.label.localizationHelp.noFiles' and loc='ja' ")
        sql("update localization set text='ラベルを選択し、ヘルプコンテンツを入力してください' where code='app.label.localizationHelp.pleaseSelect' and loc='ja' ")
        sql("update localization set text='新着情報' where code='app.label.localizationHelp.releaseNote.delete' and loc='ja' ")
        sql("update localization set text='新着情報' where code='app.label.localizationHelp.releaseNoteItem.delete' and loc='ja' ")
        sql("update localization set text='PV Report - 新着情報' where code='app.label.localizationHelp.releaseNotes.pageTitle' and loc='ja' ")
        sql("update localization set text='リリース番号' where code='app.label.localizationHelp.releaseNotes.releaseNumber' and loc='ja' ")
        sql("update localization set text='敬称' where code='app.label.localizationHelp.title' and loc='ja' ")
        sql("update localization set text='PV Report - リリースノートの作成' where code='app.label.localizationHelp.createReleaseNote.title' and loc='ja' ")
        sql("update localization set text='リリースノートの作成' where code='app.label.localizationHelp.createReleaseNote' and loc='ja' ")
        sql("update localization set text='敬称' where code='app.label.localizationHelp.releaseNotes.title' and loc='ja' ")
        sql("update localization set text='詳細' where code='app.label.localizationHelp.releaseNotes.description' and loc='ja' ")
        sql("update localization set text='PV Report - リリースノートの作成' where code='app.label.releaseNotes.create.title' and loc='ja' ")
        sql("update localization set text='PV Report - リリースノートの編集' where code='app.label.releaseNotes.edit.title' and loc='ja' ")
        sql("update localization set text='PV Report - リリースノートの表示' where code='app.label.releaseNotes.view.title' and loc='ja' ")
        sql("update localization set text='リリースノートの編集' where code='app.label.releaseNotes.edit' and loc='ja' ")
        sql("update localization set text='新しいリリースノートアイテムの作成' where code='app.label.releaseNotesItem.create' and loc='ja' ")
        sql("update localization set text='新しいバージョンがインストールされました' where code='app.label.whatsNewReminder.title' and loc='ja' ")
        sql("update localization set text='アプリケーションを更新しました。新機能について確認されますか？' where code='app.label.whatsNewReminder.message' and loc='ja' ")
        sql("update localization set text='後ほど再通知' where code='app.label.whatsNewReminder.later' and loc='ja' ")
        sql("update localization set text='次回から表示しない' where code='app.label.whatsNewReminder.dont' and loc='ja' ")
        sql("update localization set text='新機能について確認する' where code='app.label.whatsNewReminder.view' and loc='ja' ")
        sql("update localization set text='新しいリリースに関する通知は、すべてのユーザーに表示されます。' where code='app.label.whatsNewReminder.sent' and loc='ja' ")
        sql("update localization set text='すべてのユーザーに通知する' where code='app.label.whatsNewReminder.button' and loc='ja' ")
        sql("update localization set text='新しいリリースと新機能についてすべてのユーザーに通知しますか?' where code='app.label.localizationHelp.confirmation' and loc='ja' ")
        sql("update localization set text='リリースノート' where code='app.label.releaseNotesItem.menu' and loc='ja' ")
        sql("update localization set text='さらに詳細を確認' where code='app.label.releaseNotesItem.learnMore' and loc='ja' ")
        sql("update localization set text='タイトル / JIRA ID / 機能名' where code='app.label.releaseNotesItem.title' and loc='ja' ")
        sql("update localization set text='機能面への影響の概要' where code='app.label.releaseNotesItem.summary' and loc='ja' ")
        sql("update localization set text='技術面への影響の概要' where code='app.label.releaseNotesItem.shortDescription' and loc='ja' ")
        sql("update localization set text='詳細な説明/トレーニング' where code='app.label.releaseNotesItem.learn' and loc='ja' ")
        sql("update localization set text='このラベルのヘルプ メッセージは既に存在します' where code='app.label.localizationHelp.unique' and loc='ja' ")
        sql("update localization set text='特定の症例＆期間リスト' where code='app.label.period.and.case.list' and loc='ja' ")
        sql("update localization set text='スキーマ比較ツール' where code='app.label.settings.schemaComparison' and loc='ja' ")
        sql("update localization set text='セクション番号 N からのレポート出力' where code='app.label.sectionOutput' and loc='ja' ")
        sql("update localization set text='セクション番号Nから出力されたレポートのテーブルのみ' where code='app.label.tableOutput' and loc='ja' ")
        sql("update localization set text='セクション番号Nから出力されたレポートからのグラフのみ' where code='app.label.chartOutput' and loc='ja' ")
        sql("update localization set text='PVR_PUBLIC_TOKENが未設定か無効です' where code='default.public.token.error.message' and loc='ja' ")
        sql("update localization set text='選択した症例バージョンには医療機器製品がありません。ICSRレポートには他の症例または別のプロファイルを選択してください。' where code='icsr.profile.manual.schedule.no.Device.error' and loc='ja' ")
        sql("update localization set text='設定管理' where code='app.label.config.management' and loc='ja' ")
        sql("update localization set text='症例データが見つかりません' where code='icsr.case.data.not.found' and loc='ja' ")
        sql("update localization set text='症例データが見つからないもしくはプロファイルが医療機器報告用ではありません' where code='icsr.profile.not.device.reportable' and loc='ja' ")
        sql("update localization set text='実行タイプを選択する必要があります' where code='com.rxlogix.config.BmQuerySection.executeFor.nullable' and loc='ja' ")
        sql("update localization set text='開始日を入力してください' where code='com.rxlogix.config.BmQuerySection.executionStartDate.nullable' and loc='ja' ")
        sql("update localization set text='終了日を入力してください' where code='com.rxlogix.config.BmQuerySection.executionEndDate.nullable' and loc='ja' ")
        sql("update localization set text='パラメータ値を指定する必要があります' where code='com.rxlogix.config.BmQuerySection.xValue.nullable' and loc='ja' ")
        sql("update localization set text='少なくとも 1 つのデータソースをに選択する必要があります' where code='com.rxlogix.config.BmQuerySection.sourceProfile.nullable' and loc='ja' ")
        sql("update localization set text='ETLを一時停止する' where code='pause.initial.etl' and loc='ja' ")
        sql("update localization set text='ETLを正常に中断しました。' where code='etl.paused.success.message' and loc='ja' ")
        sql("update localization set text='ETLの一時停止に失敗しました。' where code='etl.request.to.paused.failed' and loc='ja' ")
        sql("update localization set text='ETL が正常に再開されました' where code='etl.resumed.success.message' and loc='ja' ")
        sql("update localization set text='ETL の再開に失敗しました' where code='etl.request.to.resumed.failed' and loc='ja' ")
        sql("update localization set text='ETLを一時停止してもよろしいですか?' where code='pause.etl.now' and loc='ja' ")
        sql("update localization set text='ETLを再開してもよろしいですか?' where code='resume.etl.now' and loc='ja' ")
        sql("update localization set text='レポート出力件数が多いため、メッセージ本文に埋め込むことはできません。電子メールに添付された出力をご確認ください。' where code='app.embed.email.message' and loc='ja' ")
        sql("update localization set text='症例シリーズのみ完了しており、レポートはまだ作成されていません' where code='app.embed.email.notGenerated' and loc='ja' ")
        sql("update localization set text='Balance Minus Query が実行中のためジョブを無効にできません' where code='balanceMinusQuery.running.disable.schedule.request.failed' and loc='ja' ")
        sql("update localization set text='Balance Minus Queryを保存するには、日付/時刻を未来日にする必要があります。' where code='save.start.date.time.balanceMinusQuery' and loc='ja' ")
        sql("update localization set text='Balance Minus Queryを更新するには、日付/時刻を未来日にする必要があります。' where code='update.start.date.time.balanceMinusQuery' and loc='ja' ")
        sql("update localization set text='サマリーは {3} 文字を超えて入力することはできません。' where code='com.rxlogix.config.TemplateQuery.summary.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='アクションは {3} 文字を超えて入力することはできません。' where code='com.rxlogix.config.TemplateQuery.actions.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='調査は {3} 文字を超えて入力することはできません。' where code='com.rxlogix.config.TemplateQuery.investigation.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='PV Quality - PVQ設定' where code='app.configuration.autorca.title' and loc='ja' ")
        sql("update localization set text='PVQ 設定' where code='app.configuration.pvqcfg.title' and loc='ja' ")
        sql("update localization set text='PV品質設定' where code='app.configuration.autorca.name' and loc='ja' ")
        sql("update localization set text='最終実行ステータス' where code='app.configuration.autorca.lastRunStatus' and loc='ja' ")
        sql("update localization set text='レポート結果の表示' where code='app.configuration.autorca.viewResults' and loc='ja' ")
        sql("update localization set text='実行中' where code='app.configuration.autorca.state.run' and loc='ja' ")
        sql("update localization set text='停止' where code='app.configuration.autorca.state.stop' and loc='ja' ")
        sql("update localization set text='PVQレポート:' where code='app.configuration.autorca.state.reportName' and loc='ja' ")
        sql("update localization set text='PVQ 設定を作成/編集する権限がありません。' where code='app.configuration.autorca.permission' and loc='ja' ")
        sql("update localization set text='PVR 設定を作成/編集する権限がありません。' where code='app.configuration.pvr.autorca.permission' and loc='ja' ")
        sql("update localization set text='優先度を上げる' where code='app.pvq.prioritizeReport' and loc='ja' ")
        sql("update localization set text='サマリーは {3} 文字を超えて入力することはできません。' where code='com.rxlogix.config.QueryRCA.summary.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='アクションは {3} 文字を超えて入力することはできません。' where code='com.rxlogix.config.QueryRCA.actions.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='調査は {3} 文字を超えて入力することはできません。' where code='com.rxlogix.config.QueryRCA.investigation.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='ETL が 「 ETL 一時停止」 により一時停止されています' where code='app.etl.pause.message' and loc='ja' ")
        sql("update localization set text='ETL は、「 ETL再開」により再開されました' where code='app.etl.resume.message' and loc='ja' ")
        sql("update localization set text='{0}  (v{1}) - {2} : ({3}) 受信者 : {4}のレポートを自動スケジュール しました' where code='auditLog.entityValue.icsr.scheduled.auto' and loc='ja' ")
        sql("update localization set text='{0} (v{1}) - {2} : ({3}) 受信者 : {4}のレポートを手動スケジュール しました' where code='auditLog.entityValue.icsr.scheduled.manual' and loc='ja' ")
        sql("update localization set text='{0}(v{1}):{2} : ({3}) 受信者 : {4}の ICSR レポートを作成しました' where code='auditLog.entityValue.icsr.generated' and loc='ja' ")
        sql("update localization set text='{0}  (v{1}) - {2} : ({3}) 受信者 : {4}の ICSR レポートを変更変更しました' where code='auditLog.entityValue.icsr.changes' and loc='ja' ")
        sql("update localization set text='{0} (v{1}) - {2} : ({3}) 受信者 : {4}の ICSR レポートを再作成しました' where code='auditLog.entityValue.icsr.regenerated' and loc='ja' ")
        sql("update localization set text='{0}(v{1}) - {2} : ({3}) 受信者 : {4}  の ICSR レポートを削除しました(理由 : {5})' where code='auditLog.entityValue.icsr.delete' and loc='ja' ")
        sql("update localization set text='時間' where code='report.submission.submissionTime' and loc='ja' ")
        sql("update localization set text='タイムゾーン' where code='report.submission.timeZone' and loc='ja' ")
        sql("update localization set text='コメントは 8000 文字を超えて入力することはできません' where code='com.rxlogix.config.caseDataQuality.comment.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='サマリーは 4000 文字を超えて入力することはできません' where code='com.rxlogix.config.actionPlan.summary.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='コメントを空欄にすることはできません' where code='com.rxlogix.config.caseDataQuality.comment.isNull' and loc='ja' ")
        sql("update localization set text='選択した報告先のレポートの送信中にエラーが発生しました。システム管理者にお問い合わせください' where code='app.periodic.report.submission.error.messsage' and loc='ja' ")
        sql("update localization set text='パスワードを入力してください' where code='app.label.workflow.rule.fill.password' and loc='ja' ")
        sql("update localization set text='入力された製品グループ名は存在しません。有効なグループ名を入力してください' where code='app.product.group.name.invalid' and loc='ja' ")
        sql("update localization set text='入力された有事事象グループは存在しません。有効なグループ名を入力してください' where code='app.event.group.name.invalid' and loc='ja' ")
        sql("update localization set text='電子メールで{0}({1})に{2}形式で送信しました。' where code='auditLog.entityValue.email' and loc='ja' ")
        sql("update localization set text='{0}を{1}形式でエクスポートしました' where code='auditLog.entityValue.bulk.export' and loc='ja' ")
        sql("update localization set text='{0}形式でエクスポートしました' where code='auditLog.entityValue.export' and loc='ja' ")
        sql("update localization set text='アクションプラン({0})をエクスポートしました' where code='auditLog.entityValue.action.plan.export' and loc='ja' ")
        sql("update localization set text='{0}のステータスを {1}から{2}へ変更しました' where code='auditLog.entityValue.workflowJustification' and loc='ja' ")
        sql("update localization set text='同じプロファイルに対して既に処理されているため、スケジュールできません' where code='icsr.add.manual.case.failure' and loc='ja' ")
        sql("update localization set text='セクションヘッダー' where code='app.label.sectionHeader' and loc='ja' ")
        sql("update localization set text='セクションフッター' where code='app.label.sectionFooter' and loc='ja' ")
        sql("update localization set text='製品グループ' where code='app.label.productGroup' and loc='ja' ")
        sql("update localization set text='PV Report - RCAマッピング' where code='app.lateMapping.title' and loc='ja' ")
        sql("update localization set text='PV Report - フィールド管理' where code='app.field.management.title' and loc='ja' ")
        sql("update localization set text='PV Report - 是正措置/予防措置設定' where code='app.capaAction.title' and loc='ja' ")
        sql("update localization set text='PV Report - カスタムフィールド設定' where code='app.customField.title' and loc='ja' ")
        sql("update localization set text='{0}グループ名は {1} 文字以内に設定してください' where code='com.rxlogix.dictionary.DictionaryGroup.groupName.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='ダッシュボードのラベルは 255 文字以内に設定してください' where code='com.rxlogix.config.Dashboard.label.maxSize.exceeded' and loc='ja' ")
        sql("update localization set text='ダッシュボードのラベルは 最低５文字以上設定してください' where code='com.rxlogix.config.Dashboard.label.minSize.notmet' and loc='ja' ")
        sql("update localization set text='一括編集用に500 行以上を選択しました。500行を超える行に変更を加える必要がある場合は、一回当たり500行を超えないように行い、それ以上実施する必要がある場合は繰り返し実施してください。' where code='app.reasonOfDelay.bulkUpdateMaxRowsWarning' and loc='ja' ")
        sql("update localization set text='OAuth メール設定' where code='app.oauth.header.title' and loc='ja' ")
        sql("update localization set text='Balance クエリと Minus クエリは既に実行されています。実行完了するまでしばらくお待ちください' where code='app.balance.minus.in.progress.msg' and loc='ja' ")
        sql("update localization set text='症例番号{0}~ {1}のオーナーを更新しました' where code='auditLog.drilldown.assignedTo.extraValue' and loc='ja' ")
        sql("update localization set text='{0}利用できません' where code='app.caseList.not.available' and loc='ja' ")
        sql("update localization set text='フッター名はすでに使用されています。別の名前を使用してください' where code='com.rxlogix.config.reportFooter.footer.unique.per.user' and loc='ja' ")
        sql("update localization set text='期限は365日以上設定することはできません' where code='com.rxlogix.config.Task.dueDate.max.exceeded' and loc='ja' ")
        sql("update localization set text='ユーザーグループ設定を確認' where code='user.group.view.label' and loc='ja' ")
        sql("update localization set text='システム通知' where code='app.label.systemNotification.systemNotification' and loc='ja' ")
        sql("update localization set text='システム通知' where code='app.label.systemNotification.systemNotifications' and loc='ja' ")
        sql("update localization set text='システム通知リスト' where code='app.label.systemNotification.list' and loc='ja' ")
        sql("update localization set text='PV Report - システム通知' where code='app.label.systemNotification.pageTitle' and loc='ja' ")
        sql("update localization set text='PV Report - システム通知の追加' where code='app.label.systemNotification.create.title' and loc='ja' ")
        sql("update localization set text='PV Report - システム通知の編集' where code='app.label.systemNotification.edit.title' and loc='ja' ")
        sql("update localization set text='PV Report - システム通知の表示' where code='app.label.systemNotification.view.title' and loc='ja' ")
        sql("update localization set text='システム通知の追加' where code='app.label.systemNotification.create' and loc='ja' ")
        sql("update localization set text='システム通知の編集' where code='app.label.systemNotification.edit' and loc='ja' ")
        sql("update localization set text='システム通知の表示' where code='app.label.systemNotification.view' and loc='ja' ")
        sql("update localization set text='詳細を表示する' where code='app.label.systemNotification.viewDetails' and loc='ja' ")
        sql("update localization set text='モーダルで通知を確認する' where code='app.label.systemNotification.test' and loc='ja' ")
        sql("update localization set text='以下のグループのユーザーのみに通知する' where code='app.label.systemNotification.groupsOnly' and loc='ja' ")
        sql("update localization set text='リリースの詳細' where code='app.label.newRelease.view' and loc='ja' ")
        sql("update localization set text='インタラクティブヘルプ' where code='app.label.interactiveHelp.interactiveHelp' and loc='ja' ")
        sql("update localization set text='ページ' where code='app.label.interactiveHelp.pages' and loc='ja' ")
        sql("update localization set text='インテラクティブヘルプリスト' where code='app.label.interactiveHelp.list' and loc='ja' ")
        sql("update localization set text='PV Report -インテラクティブヘルプ' where code='app.label.interactiveHelp.pageTitle' and loc='ja' ")
        sql("update localization set text='PV Report-インタラクティブヘルプを追加' where code='app.label.interactiveHelp.create.title' and loc='ja' ")
        sql("update localization set text='PV Report-インタラクティブヘルプを編集' where code='app.label.interactiveHelp.edit.title' and loc='ja' ")
        sql("update localization set text='PV Report-インタラクティブヘルプを表示' where code='app.label.interactiveHelp.view.title' and loc='ja' ")
        sql("update localization set text='インタラクティブヘルプの追加' where code='app.label.interactiveHelp.create' and loc='ja' ")
        sql("update localization set text='インタラクティブヘルプの編集' where code='app.label.interactiveHelp.edit' and loc='ja' ")
        sql("update localization set text='インタラクティブヘルプの表示' where code='app.label.interactiveHelp.view' and loc='ja' ")
        sql("update localization set text='PV Reports - デフォルトのヘルプリンク' where code='app.label.localizationHelp.helpLink.title' and loc='ja' ")
        sql("update localization set text='デフォルトのヘルプリンク' where code='app.label.localizationHelp.helpLink' and loc='ja' ")
        sql("update localization set text='フィールドプロファイルを表示' where code='app.view.field.profile.label' and loc='ja' ")
        sql("update localization set text='フィールドプロファイルの編集' where code='app.edit.field.profile.label' and loc='ja' ")
        sql("update localization set text='ICSRレポートのダウンロードに失敗しました' where code='icsr.report.download.error' and loc='ja' ")
        sql("update localization set text='ICSR一括ダウンロードジョブ' where code='com.rxlogix.jobs.BulkDownloadIcsrReportsJob' and loc='ja' ")
        sql("update localization set text='ユーザーグループ設定を確認' where code='app.label.user.group.view' and loc='ja' ")
        sql("update localization set text='PV Reports - ダッシュボードライブラリ' where code='app.DashboardLibrary.title' and loc='ja' ")
        sql("update localization set text='ICSR症例リストの抽出に成功しました' where code='icsr.public.api.case.list.fetch.success' and loc='ja' ")
        sql("update localization set text='このページを閲覧する権限がありません。' where code='icsr.public.api.user.unauthorized' and loc='ja' ")
        sql("update localization set text='テナントIDが入力されていません' where code='icsr.public.api.tenantId.missing' and loc='ja' ")
        sql("update localization set text='FU #' where code='app.label.followUpType' and loc='ja' ")
        sql("update localization set text='FUタイプ' where code='app.label.followUpInfo' and loc='ja' ")
        sql("update localization set text='PV Reports - ダッシュボードライブラリ' where code='app.dashboardLibrary.title' and loc='ja' ")
        sql("update localization set text='\"レポートを作成中です。時間をおいてページを再読み込みしてください（完了後に通知されます）\"' where code='app.reasonOfDelay.loading.warn' and loc='ja' ")
        sql("update localization set text='品質イシューの詳細で(#;''<\">)などの特殊記号を使用することはできません' where code='app.pvq.qualityIssueDescription.validation.note' and loc='ja' ")
        sql("update localization set text='行 {0} : ユーザー名が入力されていません' where code='app.bulkUpdate.error.userName.does.not.exist' and loc='ja' ")
        sql("update localization set text='行 {0} : テナントIDが入力されていません' where code='app.bulkUpdate.error.tenant.does.not.exist' and loc='ja' ")
        sql("update localization set text='ユーザー名' where code='app.label.username' and loc='ja' ")
        sql("update localization set text='警告\\：テンプレートが存在しません。削除された可能性があります。' where code='app.template.warn.isDeleted' and loc='ja' ")
        sql("update localization set text='既に本症例のICSRは作成済です' where code='icsr.generate.manual.generated' and loc='ja' ")
        sql("update localization set text='ユーザーグループの新規作成' where code='app.label.usergroup.create' and loc='ja' ")
        sql("update localization set text='レポートフィールドの新規作成' where code='app.label.report.field.create' and loc='ja' ")
        sql("update localization set text='ダッシュボードの新規作成' where code='app.label.dashboard.create' and loc='ja' ")
        sql("update localization set text='フィールドプロファイルの新規作成' where code='app.label.field.profile.create' and loc='ja' ")
        sql("update localization set text='カスタムフィールドの新規作成' where code='app.label.custom.field.create' and loc='ja' ")
        sql("update localization set text='Eメールテンプレートの新規作成' where code='app.label.email.template.create' and loc='ja' ")
        sql("update localization set text='Eメール設定の新規作成' where code='app.label.email.configuration.create' and loc='ja' ")
        sql("update localization set text='フッター設定の新規作成' where code='app.label.footer.configuration.create' and loc='ja' ")
        sql("update localization set text='PVQ設定の新規作成' where code='app.label.pvq.configuration.create' and loc='ja' ")
        sql("update localization set text='PVQレポート' where code='app.configuration.pvqcfg.title.create' and loc='ja' ")
        sql("update localization set text='PVQレポートの編集' where code='app.configuration.pvqcfg.title.edit' and loc='ja' ")
        sql("update localization set text='PVQ品質コンフィグレーションライブラリ' where code='app.configuration.pvrcfg.title.name' and loc='ja' ")
        sql("update localization set text='PVC：自身もしくは所属グループにアサインされた根本原因のみを表示する' where code='app.role.ROLE_USER_GROUP_RCA' and loc='ja' ")
        sql("update localization set text='PVQ：自身もしくは所属グループにアサインされた根本原因のみを表示する' where code='app.role.ROLE_USER_GROUP_RCA_PVQ' and loc='ja' ")
        sql("update localization set text='ファイルが空です' where code='app.import.file.empty' and loc='ja' ")
        sql("update localization set text='Case#{0} v{1}: {2} profileのICSRレポートを破棄しました。' where code='icsr.public.api.nullify.report.success' and loc='ja' ")
        sql("update localization set text='治験措置、研究報告' where code='app.icsr.rule.evaluation.CLINICAL_RESEARCH_MEASURE_REPORT' and loc='ja' ")

    }

    changeSet(author: 'Shivam', id: '202501291648') {
        sql("update localization set text='サブエンティティ' where code='audit.parent.label' and loc='ja' ")
    }

    changeSet(author: 'Siddharth', id: '20250210165608-07') {
        sql("update localization set text='スケジュール済み' where code='app.executionStatus.dropDown.SCHEDULED' and loc='ja' ")
    }

    changeSet(author: 'meenal', id: '202502141223-01') {
        sql("update localization set text='F/U #' where code='app.label.followUpType' and loc='ja'")
        sql("update localization set text='F/Uタイプ' where code='app.label.followUpInfo' and loc='ja'")
    }
  
    changeSet(author: 'gunjan', id: '202502141839') {
        sql("update localization set text='Automatically assign to users within group(s)' where code='app.label.workflow.rule.autoAssignToUsers' and loc='*' ")
    }

    changeSet(author: 'Vivek', id: '202502181416-1') {
        sql("update localization set text='製品の評価区分' where code='app.label.icsr.profile.conf.iscr.product.evaluation' and loc='ja' ")
        sql("update localization set text='医療機器の不具合報告' where code='app.icsr.rule.evaluation.DEVICE_REPORTING' and loc='ja' ")
        sql("update localization set text='製品レベル' where code='app.icsr.rule.evaluation.PRODUCT_LEVEL' and loc='ja' ")
    }

    changeSet(author: 'vivek', id: '202503031625') {
        sql("update localization set text='Case#{0} v{1}: Successfully marked as Submitted for profile {2}.' where code='app.reportSubmission.submitted.successful'")
    }
  
    changeSet(author: 'Siddharth', id: '20250303170032-7') {
        sql("update localization set text='提出済' where code='app.reportSubmissionStatus.SUBMITTED' and loc='ja' ")
        sql("update localization set text='提出不要' where code='app.reportSubmissionStatus.SUBMISSION_NOT_REQUIRED' and loc='ja' ")
        sql("update localization set text='未提出' where code='app.reportSubmissionStatus.PENDING' and loc='ja' ")
    }

    changeSet(author: 'vivek', id: '202503051855') {
        sql("update localization set text='Access Denied! You do not have permission to view that page.' where code='error.403.message' and loc='*'")
    }

    changeSet(author: 'meenal', id: '202503121807-01') {
        sql("update localization set text='Please enter a valid ICSR Report Submission Date that is on or after the report generation date and not in the future.' where code='submission.date.later.error'")
    }

    changeSet(author: 'shivam', id: '202503131225-01') {
        sql("update localization set text='繰り返し間隔' where code='scheduler.repeat' and loc='ja' ")
        sql("update localization set text='時' where code='scheduler.hourly' and loc='ja' ")
        sql("update localization set text='日' where code='scheduer.daily' and loc='ja' ")
        sql("update localization set text='週' where code='scheduler.weekly' and loc='ja' ")
        sql("update localization set text='月' where code='scheduler.monthly' and loc='ja' ")
        sql("update localization set text='年' where code='scheduler.yearly' and loc='ja' ")
        sql("update localization set text=' ' where code='scheduler.every' and loc='ja' ")
        sql("update localization set text='年ごと' where code='scheduler.years' and loc='ja' ")
        sql("update localization set text='か月ごと' where code='scheduler.months' and loc='ja' ")
        sql("update localization set text='週間ごと' where code='scheduler.weeks' and loc='ja' ")
        sql("update localization set text='日ごと' where code='scheduler.days' and loc='ja' ")
        sql("update localization set text='時間ごと' where code='scheduler.hours' and loc='ja' ")
    }

    changeSet(author: 'shivam', id: '202503131430-01') {
        sql("update localization set text='なし(1回のみ)' where code='scheduler.none.run.once' and loc='ja' ")
    }

    changeSet(author: 'gunjan', id: '202503171430') {
        sql("update localization set text='If Check Box is Checked, then Case creation date will be used when safety receipt date is not populated.<br>If Check Box is Unchecked, then only safety receipt date will be considered and if the date is null then the case will not be qualified' where code='controlPanel.inboundCompliance.caseDateLogic.help' and loc='*' ")
    }

    changeSet(author: 'vivek', id: '202504031257-1') {
        sql("update localization set text='症例#{0} v{1}: {2}プロファイルのレポートを正常に提出不要に変更しました。' where code='app.reportSubmission.submission.not.req.successful' and loc='ja' ")
        sql("update localization set text='提出に関する詳細' where code='app.label.submission.details' and loc='ja' ")
        sql("update localization set text='症例#{0} v{1}: {2}プロファイルのレポートを正常に提出済に変更しました。' where code='app.reportSubmission.submitted.successful' and loc='ja' ")
    }

    changeSet(author: 'meenal', id: '202504041504-01') {
        sql("update localization set text='表示' where code='icsr.case.tracking.actions.view' and loc='ja' ")
    }


    changeSet(author: 'gunjan', id: '202504101026') {
        sql("update localization set text='If a case number is listed more than once in Associated case number section, then there are multiple submissions for that case which are to be assessed' where code='app.email.pvc.pvq.note' and loc='*' ")
    }
    changeSet(author: 'meenal', id: '202504101209-01') {
        sql("update localization set text='治験/市販後区分' where code='app.label.icsr.profile.conf.authorizationType' and loc='ja' ")

    }

    changeSet(author: 'gunjan', id: '202504131426') {
        sql("update localization set text='If a case number is listed more than once in Associated Case Number section, then there are multiple submissions for that case which are to be assessed' where code='app.email.pvc.pvq.note' and loc='*' ")
    }

    changeSet(author: 'Siddharth', id: '202500415143051-07') {
        sql("update localization set text='薬品コード/治験成分記号' where code='app.label.icsr.profile.conf.case.approvalNumber' and loc='ja' ")
        sql("update localization set text='薬品コード/治験成分記号' where code='icsr.profile.manual.approval.number' and loc='ja' ")
    }

    changeSet(author: 'meenal', id: '202504171201-01') {
        sql("update localization set text='定期報告' where code='app.periodicReports.generated.tittle' and loc='ja' ")
        sql("update localization set text='定期報告' where code='app.label.periodicReport' and loc='ja' ")
        sql("update localization set text='定期報告' where code='app.configurationType.PERIODIC_REPORT' and loc='ja' ")
        sql("update localization set text='定期報告' where code='app.workflowConfigurationType.PERIODIC_REPORT' and loc='ja' ")
        sql("update localization set text='定期報告' where code='app.actionItemCategory.PERIODIC_REPORT' and loc='ja' ")
    }

    changeSet(author: 'sahil',id:'202504211718-01'){
        sql("update localization set text='ETL is running longer' where code='app.emailService.elt.running.default.email.body' and loc='*'")
    }

    changeSet(author: 'Siddharth', id: '20250425183930-07') {
        sql("update localization set text='ICSRトラッキング' where code='app.label.view.cases' and loc='ja' ")
        sql("update localization set text='ICSRトラッキング' where code='iscr.case.tracking.label' and loc='ja' ")
        sql("update localization set text='ICSRトラッキング' where code='app.label.icsr.case.tracking' and loc='ja' ")
        sql("update localization set text='プロファイル' where code='icsr.case.tracking.profile' and loc='ja' ")
        sql("update localization set text='添付' where code='app.label.attach' and loc='ja' ")
        sql("update localization set text='ローカルレポート/メッセージ #:' where code='app.label.localReportMsg' and loc='ja' ")
        sql("update localization set text='提出不要' where code='icsr.case.tracking.actions.labelNotSubmit' and loc='ja' ")
        sql("update localization set text='症例データを添付の上、メール送信しました。症例番号 {0} v: {1}' where code='icsr.case.email.sent.success' and loc='ja' ")
        sql("update localization set text='症例＃{0} V{1}: {2} のICSRレポートを正常に破棄しました' where code='icsr.public.api.nullify.report.success' and loc='ja' ")
        sql("update localization set text='ご担当者様、' where code='app.label.hi' and loc='ja' ")
    }

    changeSet(author: 'sahil',id:'202505081418-01'){
        sql("update localization set text='提出処理を正常に完了しました' where code='app.reportSubmission.submitted.report.successful' and loc='ja'")
    }

    changeSet(author: 'meenal',id:'202505121441-01'){
        sql("update localization set text='選択されたプロファイルとケース（バージョン）の組み合わせに対するICSRレポートのプレビュー/生成は、すでに進行中または完了しています。' where code='icsr.generate.manual.generating.or.generated' and loc='ja'")
        sql("update localization set text='標準的な正当化リストの取得中にエラーが発生しました。' where code='icsr.standard.justification.failure' and loc='ja'")
        sql("update localization set text='標準的な正当化リストを正常に取得しました。' where code='icsr.standard.justification.success' and loc='ja'")
        sql("update localization set text='アクションが見つかりません。' where code='icsr.public.api.action.missing' and loc='ja'")
        sql("update localization set text='ユーザー設定の更新中にエラーが発生しました。' where code='icsr.user.preference.error' and loc='ja'")
        sql("update localization set text='ユーザー設定の更新が正常に完了しました。' where code='icsr.user.preference.success' and loc='ja'")
        sql("update localization set text='ユーザー設定が見つかりません。' where code='icsr.user.Preference.required' and loc='ja'")
        sql("update localization set text='ユーザー設定の取得中にエラーが発生しました。' where code='icsr.user.fetch.preference.error' and loc='ja'")
        sql("update localization set text='ユーザー設定を正常に取得しました。' where code='icsr.user.fetch.preference.success' and loc='ja'")
        sql("update localization set text='ユーザー設定キーが見つかりません。' where code='icsr.user.preference.key.error' and loc='ja'")
        sql("update localization set text='有効なICSRレポート提出日を入力してください。提出日はレポート生成日以降であり、未来の日付であってはなりません。' where code='submission.date.later.error' and loc='ja'")
        sql("update localization set text='認証リストを正常に取得しました。' where code='icsr.public.api.authorization.list.success' and loc='ja'")
    }

    changeSet(author: 'meenal',id:'202505161144-01'){
        sql("update localization set text='報告対象外' where code='app.icsr.report.caseList.downgrade' and loc='ja'")
        sql("update localization set text='報告対象外' where code='icsr.case.tracking.downgrade' and loc='ja'")
        sql("update localization set text='報告対象外' where code='app.followupInfo.Downgrade' and loc='ja'")
    }

    changeSet(author: "farhan", id: "202505081912-1"){
        sql("update localization set text='Login failed. Please check your credentials and try again.' where code='springSecurity.errors.login.fail'")
        sql("update localization set text='The description contains invalid or unsafe characters.' where code='com.rxlogix.config.Configuration.description.invalid.content'")
        sql("update localization set text='The report name contains invalid or unsafe characters.' where code='com.rxlogix.config.Configuration.reportName.invalid.content'")
    }

}
