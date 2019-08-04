using my.bookshop as my from '../db/data-model';


service CatalogService {
    @readonly entity Books as projection on my.Books;
    action updateStock ( bookId:Integer, multiplier:Integer );
    action callProc ( action: Integer);
}
