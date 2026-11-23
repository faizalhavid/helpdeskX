


interface BaseModel {
    id: string;

    createdAt : Date | string;
    updatedAt : Date | string;
    isDeleted : boolean;
    deletedAt?: Date | string | null;
}