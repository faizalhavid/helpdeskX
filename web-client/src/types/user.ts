




interface User extends BaseModel{
    id: string;
    username : string;
    email : string;
    role : string;
    userGroup : string;
    
    createdAt : Date;
    updatedAt : Date;
}