import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { NumeroInventaireFormService, NumeroInventaireFormGroup } from './numero-inventaire-form.service';
import { INumeroInventaire } from '../numero-inventaire.model';
import { NumeroInventaireService } from '../service/numero-inventaire.service';
import { IMateriel } from 'app/entities/materiel/materiel.model';
import { MaterielService } from 'app/entities/materiel/service/materiel.service';
import { ICollaborateurs } from 'app/entities/collaborateurs/collaborateurs.model';
import { CollaborateursService } from 'app/entities/collaborateurs/service/collaborateurs.service';

@Component({
  selector: 'jhi-numero-inventaire-update',
  templateUrl: './numero-inventaire-update.component.html',
})
export class NumeroInventaireUpdateComponent implements OnInit {
  isSaving = false;
  numeroInventaire: INumeroInventaire | null = null;

  materielsSharedCollection: IMateriel[] = [];
  collaborateursSharedCollection: ICollaborateurs[] = [];

  editForm: NumeroInventaireFormGroup = this.numeroInventaireFormService.createNumeroInventaireFormGroup();

  constructor(
    protected numeroInventaireService: NumeroInventaireService,
    protected numeroInventaireFormService: NumeroInventaireFormService,
    protected materielService: MaterielService,
    protected collaborateursService: CollaborateursService,
    protected activatedRoute: ActivatedRoute
  ) {}

  compareMateriel = (o1: IMateriel | null, o2: IMateriel | null): boolean => this.materielService.compareMateriel(o1, o2);

  compareCollaborateurs = (o1: ICollaborateurs | null, o2: ICollaborateurs | null): boolean =>
    this.collaborateursService.compareCollaborateurs(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ numeroInventaire }) => {
      this.numeroInventaire = numeroInventaire;
      if (numeroInventaire) {
        this.updateForm(numeroInventaire);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const numeroInventaire = this.numeroInventaireFormService.getNumeroInventaire(this.editForm);
    if (numeroInventaire.id !== null) {
      this.subscribeToSaveResponse(this.numeroInventaireService.update(numeroInventaire));
    } else {
      this.subscribeToSaveResponse(this.numeroInventaireService.create(numeroInventaire));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<INumeroInventaire>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(numeroInventaire: INumeroInventaire): void {
    this.numeroInventaire = numeroInventaire;
    this.numeroInventaireFormService.resetForm(this.editForm, numeroInventaire);

    this.materielsSharedCollection = this.materielService.addMaterielToCollectionIfMissing<IMateriel>(
      this.materielsSharedCollection,
      numeroInventaire.materielActuel,
      numeroInventaire.ancienMateriel
    );
    this.collaborateursSharedCollection = this.collaborateursService.addCollaborateursToCollectionIfMissing<ICollaborateurs>(
      this.collaborateursSharedCollection,
      numeroInventaire.ancienProprietaire,
      numeroInventaire.nouveauProprietaire
    );
  }

  protected loadRelationshipsOptions(): void {
    this.materielService
      .query()
      .pipe(map((res: HttpResponse<IMateriel[]>) => res.body ?? []))
      .pipe(
        map((materiels: IMateriel[]) =>
          this.materielService.addMaterielToCollectionIfMissing<IMateriel>(
            materiels,
            this.numeroInventaire?.materielActuel,
            this.numeroInventaire?.ancienMateriel
          )
        )
      )
      .subscribe((materiels: IMateriel[]) => (this.materielsSharedCollection = materiels));

    this.collaborateursService
      .query()
      .pipe(map((res: HttpResponse<ICollaborateurs[]>) => res.body ?? []))
      .pipe(
        map((collaborateurs: ICollaborateurs[]) =>
          this.collaborateursService.addCollaborateursToCollectionIfMissing<ICollaborateurs>(
            collaborateurs,
            this.numeroInventaire?.ancienProprietaire,
            this.numeroInventaire?.nouveauProprietaire
          )
        )
      )
      .subscribe((collaborateurs: ICollaborateurs[]) => (this.collaborateursSharedCollection = collaborateurs));
  }
}
