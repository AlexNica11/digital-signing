import {NavLink} from "react-router-dom";

export default function ProfilePage(){

    return (
        <>
            <h1>Profile</h1>
            <div>
                <NavLink to={"/uploadKeystore"}>Upload KeyStore</NavLink>
            </div>
        </>
    );
}
