package org.codequistify.master.virtualworkspace.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.domain.shared.stage.StageCode;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageImageType;
import org.codequistify.master.domain.stage.service.StageSearchService;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.virtualworkspace.application.port.VirtualWorkspaceRepository;
import org.codequistify.master.virtualworkspace.application.port.VirtualWorkspaceClient;
import org.codequistify.master.virtualworkspace.config.VirtualWorkspaceDefaults;
import org.codequistify.master.virtualworkspace.domain.model.StageSpecSnapshot;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspace;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspaceInternalRoute;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspacePublicEndpoint;
import org.codequistify.master.virtualworkspace.domain.model.WorkspaceAccessPolicy;
import org.codequistify.master.virtualworkspace.domain.vo.SubjectId;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceConnectionResponse;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceStatusResponse;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VirtualWorkspaceServiceTest {

  @Mock
  private VirtualWorkspaceRepository virtualWorkspaceRepository;

  @Mock
  private VirtualWorkspaceClient virtualWorkspaceClient;

  @Mock
  private StageSearchService stageSearchService;

  @Mock
  private Player player;

  @Mock
  private Stage stage;

  private VirtualWorkspaceService service;

  @BeforeEach
  void setUp() {
    service = new VirtualWorkspaceService(
        virtualWorkspaceRepository,
        virtualWorkspaceClient,
        stageSearchService);
  }

  @Test
  void recreate에서_새_워크스페이스인_경우에는_running으로_저장되고_연결정보를_반환해야한다() {
    when(player.id()).thenReturn(PlayerId.of(1L));
    when(player.getUsername()).thenReturn("user-1");
    when(stageSearchService.getStageByCode(StageCode.from("S1015"))).thenReturn(stage);
    when(stage.getStageImage()).thenReturn(StageImageType.S1015);
    when(virtualWorkspaceRepository.findById(any())).thenReturn(Optional.empty());
    when(virtualWorkspaceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    VirtualWorkspaceConnectionResponse response = service.recreate(" S1015 ", player);

    assertEquals(21, response.publicId().length());
    assertEquals(response.publicId() + "." + VirtualWorkspaceDefaults.BASE_HOST, response.publicHost());
    assertEquals("wss://" + response.publicHost(), response.websocketUrl());
  }

  @Test
  void recreate에서_기존_워크스페이스가_있으면_삭제_후_재생성해야한다() {
    when(player.id()).thenReturn(PlayerId.of(1L));
    when(player.getUsername()).thenReturn("user-1");
    when(stageSearchService.getStageByCode(StageCode.from("S1015"))).thenReturn(stage);
    when(stage.getStageImage()).thenReturn(StageImageType.S1015);
    when(virtualWorkspaceRepository.findById(any()))
        .thenReturn(Optional.of(existingWorkspace()));
    when(virtualWorkspaceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    VirtualWorkspaceConnectionResponse response = service.recreate("S1015", player);

    assertEquals(21, response.publicId().length());
    assertEquals(response.publicId() + "." + VirtualWorkspaceDefaults.BASE_HOST, response.publicHost());
    assertEquals("wss://" + response.publicHost(), response.websocketUrl());
  }

  @Test
  void recreate에서_k8s_오류가_발생하면_생성실패_예외가_발생해야한다() {
    when(player.id()).thenReturn(PlayerId.of(1L));
    when(player.getUsername()).thenReturn("user-1");
    when(stageSearchService.getStageByCode(StageCode.from("S1015"))).thenReturn(stage);
    when(stage.getStageImage()).thenReturn(StageImageType.S1015);
    when(virtualWorkspaceRepository.findById(any())).thenReturn(Optional.empty());
    when(virtualWorkspaceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    doThrow(new RuntimeException("boom")).when(virtualWorkspaceClient).provision(any());

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> service.recreate("S1015", player));

    assertEquals(ErrorCode.VIRTUAL_WORKSPACE_CREATE_FAILED, exception.getErrorCode());
  }

  @Test
  void getConnection에서_워크스페이스가_없으면_예외가_발생해야한다() {
    when(player.id()).thenReturn(PlayerId.of(1L));
    when(virtualWorkspaceRepository.findById(any())).thenReturn(Optional.empty());

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> service.getConnection("S1015", player));

    assertEquals(ErrorCode.VIRTUAL_WORKSPACE_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void getStatus에서_워크스페이스가_있으면_상태와_존재여부를_반환해야한다() {
    when(player.id()).thenReturn(PlayerId.of(1L));
    VirtualWorkspace running = runningWorkspace();
    when(virtualWorkspaceRepository.findById(any())).thenReturn(Optional.of(running));
    when(virtualWorkspaceClient.exists(running.publicId())).thenReturn(true);

    VirtualWorkspaceStatusResponse response = service.getStatus("S1015", player);

    assertEquals(running.publicId().value(), response.publicId());
    assertEquals(running.lifecycle().status().name(), response.status());
    assertTrue(response.exists());
  }

  @Test
  void getStatus에서_워크스페이스가_없으면_NOT_FOUND를_반환해야한다() {
    when(player.id()).thenReturn(PlayerId.of(1L));
    when(virtualWorkspaceRepository.findById(any())).thenReturn(Optional.empty());

    VirtualWorkspaceStatusResponse response = service.getStatus("S1015", player);

    assertEquals("", response.publicId());
    assertEquals("NOT_FOUND", response.status());
    assertFalse(response.exists());
  }

  @Test
  void getSummary는_getStatus_결과를_그대로_전달해야한다() {
    when(player.id()).thenReturn(PlayerId.of(1L));
    when(virtualWorkspaceRepository.findById(any())).thenReturn(Optional.empty());

    VirtualWorkspaceSummaryResponse response = service.getSummary("S1015", player);

    assertEquals("", response.publicId());
    assertEquals("NOT_FOUND", response.status());
    assertFalse(response.exists());
  }

  private VirtualWorkspace existingWorkspace() {
    VirtualWorkspaceId workspaceId = VirtualWorkspaceId.of(PlayerId.of(1L), StageCode.from("S1015"));
    StageSpecSnapshot spec = StageSpecSnapshot.of(
        StageCode.from("S1015"),
        StageImageType.S1015.getImageName(),
        VirtualWorkspaceDefaults.SERVICE_PORT,
        VirtualWorkspaceDefaults.DEFAULT_CPU,
        VirtualWorkspaceDefaults.DEFAULT_MEMORY,
        VirtualWorkspaceDefaults.READINESS_PATH);
    WorkspaceAccessPolicy accessPolicy = WorkspaceAccessPolicy.ownerOnly(SubjectId.from("user-1"));
    VirtualWorkspacePublicEndpoint endpoint =
        VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-old"), VirtualWorkspaceDefaults.BASE_HOST);

    return VirtualWorkspace.creating(workspaceId, WorkspacePublicId.from("vw-old"), spec, endpoint, accessPolicy);
  }

  private VirtualWorkspace runningWorkspace() {
    VirtualWorkspace existing = existingWorkspace();
    VirtualWorkspaceInternalRoute internalRoute =
        VirtualWorkspaceInternalRoute.of(VirtualWorkspaceDefaults.NAMESPACE, "vw-old", VirtualWorkspaceDefaults.SERVICE_PORT);

    return existing.markRunning(internalRoute);
  }
}
