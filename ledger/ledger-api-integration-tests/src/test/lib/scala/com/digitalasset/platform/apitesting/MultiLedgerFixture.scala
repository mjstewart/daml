// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.platform.apitesting

import com.digitalasset.ledger.api.testing.utils.{LedgerBackend, MultiResourceBase, Resource}
import com.digitalasset.platform.PlatformApplications
import com.digitalasset.platform.apitesting.LedgerFactories.SandboxStore
import com.digitalasset.platform.esf.TestExecutionSequencerFactory
import org.scalatest.AsyncTestSuite

trait MultiLedgerFixture
    extends MultiResourceBase[LedgerBackend, LedgerContext]
    with TestExecutionSequencerFactory {
  self: AsyncTestSuite =>

  protected type Config = PlatformApplications.Config

  protected def Config: PlatformApplications.Config.type = PlatformApplications.Config

  protected def config: Config

  protected def basePort = 6865

  /** Overriding this provides an easy way to narrow down testing to a single implementation. */
  override protected def fixtureIdsEnabled: Set[LedgerBackend] =
    Set(LedgerBackend.SandboxInMemory, LedgerBackend.SandboxSql)

  override protected def constructResource(
      index: Int,
      fixtureId: LedgerBackend): Resource[LedgerContext] = {
    fixtureId match {
      case LedgerBackend.SandboxInMemory =>
        LedgerFactories.createSandboxResource(config, SandboxStore.InMemory)
      case LedgerBackend.SandboxSql =>
        LedgerFactories.createSandboxResource(config, SandboxStore.Postgres)
      case LedgerBackend.RemoteApiProxy =>
        LedgerFactories.createRemoteApiProxyResource(config)
    }
  }
}
