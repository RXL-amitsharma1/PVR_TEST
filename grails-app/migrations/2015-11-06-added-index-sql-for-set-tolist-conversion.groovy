databaseChangeLog = {

    changeSet(author: "Prakriti Khanal", id:'added index to queries_qrs_exp_values') {
        sql("merge into queries_qrs_exp_values t using (select query_id, query_exp_value_id, row_number() over (partition by query_id order by query_exp_value_id) - 1 value from queries_qrs_exp_values where query_expression_values_idx is null) s on (t.query_id = s.query_id and t.query_exp_value_id = s.query_exp_value_id) when matched then update set t.query_expression_values_idx = s.value;")
    }

    changeSet(author: "Prakriti Khanal", id:'added index to ex_templt_qrs_ex_query_values') {
        sql("merge into ex_templt_qrs_ex_query_values t using (select ex_templt_query_id, ex_query_value_id, row_number() over (partition by ex_templt_query_id order by ex_query_value_id) - 1 value from ex_templt_qrs_ex_query_values where ex_query_value_idx is null) s on (t.ex_templt_query_id = s.ex_templt_query_id and t.ex_query_value_id = s.ex_query_value_id) when matched then update set t.ex_query_value_idx = s.value;")
    }

    changeSet(author: "Prakriti Khanal", id:'added index to templt_qrs_query_values') {
        sql("merge into templt_qrs_query_values t using (select templt_query_id, query_value_id, row_number() over (partition by templt_query_id order by query_value_id) - 1 value from templt_qrs_query_values where query_value_idx is null) s on (t.templt_query_id = s.templt_query_id and t.query_value_id = s.query_value_id) when matched then update set t.query_value_idx = s.value;")
    }

    changeSet(author: "Prakriti Khanal", id:'added index to VALUES_PARAMS') {
        sql("merge into VALUES_PARAMS t using (select value_id, param_id, row_number() over (partition by value_id order by param_id) - 1 value from VALUES_PARAMS where param_idx is null) s on (t.value_id = s.value_id and t.param_id = s.param_id) when matched then update set t.param_idx = s.value;")
    }
}
