package com.fr.swift.jdbc.request;

import com.fr.swift.api.server.response.ApiResponse;
import com.fr.swift.jdbc.rpc.JdbcExecutor;

/**
 * @author yee
 * @date 2018-12-07
 */
public interface JdbcRequestService {
    /**
     * execute auth
     *
     * @param sender an operator to send rpc request
     * @param user swift username
     * @param password swift password
     * @return response of the request
     */
    ApiResponse apply(JdbcExecutor sender, String user, String password);

    /**
     * execute auth with retry
     *
     * @param sender an operator to send rpc request
     * @param user swift username
     * @param password swift password
     * @param retryTime retry time
     * @return response of the request
     */
    ApiResponse applyWithRetry(JdbcExecutor sender, String user, String password, int retryTime);

    /**
     * execute query
     *
     * @param sender      an operator to send rpc request
     * @param requestJson request json
     * @return response of the request
     */
    ApiResponse apply(JdbcExecutor sender, String requestJson);

    /**
     * execute query
     *
     * @param sender      an operator to send rpc request
     * @param requestJson request json
     * @param retryTime   retry time of this request.
     * @return response of the request
     */
    ApiResponse applyWithRetry(JdbcExecutor sender, String requestJson, int retryTime);
}
