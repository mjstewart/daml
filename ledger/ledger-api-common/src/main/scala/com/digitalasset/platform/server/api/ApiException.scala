// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.platform.server.api

import io.grpc.{Status, StatusRuntimeException}

import scala.util.control.NoStackTrace

/** The sole purpose of this class is to give StatusRuntimeException with NoStacktrace a nice name in logs. */
class ApiException(status: Status) extends StatusRuntimeException(status) with NoStackTrace
